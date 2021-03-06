/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greyfox.rxnetwork.internal.strategy.network.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.helpers.robolectric.shadows.ShadowConnectivityManagerWithCallback;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.M;
import static android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SuppressWarnings("ConstantConditions")
@RequiresApi(api = Build.VERSION_CODES.M)
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = M, shadows = ShadowConnectivityManagerWithCallback.class)
public class MarshmallowNetworkObservingStrategyTest {

  private final TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();
  private final RxNetworkInfo defaultRxNetworkInfo = RxNetworkInfo.create();

  @Rule public MockitoRule rule = MockitoJUnit.rule();
  @Mock private PowerManager powerManager;

  private Context context;
  private BaseNetworkObservingStrategy sut;

  @Before
  public void setUp() {
    context = spy(RuntimeEnvironment.application.getApplicationContext());
    sut = spy(new MarshmallowNetworkObservingStrategy(context));
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToInstantiateWithNullContext() {
    new MarshmallowNetworkObservingStrategy(null);
  }

  @Test()
  public void shouldRegisterWithDefaultNetworkRequest() {
    NetworkRequest defaultRequest = new NetworkRequest.Builder().build();
    ConnectivityManager manager = setUpManagerWithNetworkRequest(null);

    sut.observe().subscribeWith(testObserver).assertSubscribed();

    verify(manager).registerNetworkCallback(eq(defaultRequest), any(NetworkCallback.class));
  }

  @Test()
  public void shouldRegisterWithCustomNetworkRequest() {
    NetworkRequest customRequest =
        new NetworkRequest.Builder().addTransportType(TRANSPORT_WIFI).build();
    ConnectivityManager manager = setUpManagerWithNetworkRequest(customRequest);

    sut.observe().subscribeWith(testObserver).assertSubscribed();

    verify(manager).registerNetworkCallback(eq(customRequest), any(NetworkCallback.class));
  }

  @Test
  public void shouldSubscribeCorrectly() {
    sut.observe().subscribeWith(testObserver);

    testObserver.assertSubscribed().assertEmpty();
  }

  @Test
  public void shouldGetValue_whenIdleModeChanged_andDeviceNotIdled() {
    setUpDeviceIdleMode(false);

    sut.observe().subscribeWith(testObserver);
    testObserver.assertSubscribed().assertEmpty();

    RuntimeEnvironment.application.sendBroadcast(new Intent(ACTION_DEVICE_IDLE_MODE_CHANGED));

    testObserver.assertSubscribed().assertValueCount(1);
  }

  @Test
  public void shouldGetValue_whenIdleModeChanged_andDeviceIdled() {
    setUpDeviceIdleMode(true);
    doReturn(false).when(powerManager).isIgnoringBatteryOptimizations(anyString());

    sut.observe().subscribeWith(testObserver);
    testObserver.assertSubscribed().assertEmpty();

    RuntimeEnvironment.application.sendBroadcast(new Intent(ACTION_DEVICE_IDLE_MODE_CHANGED));

    testObserver.assertSubscribed().assertValue(defaultRxNetworkInfo);
  }

  @Test
  public void shouldDisposeCorrectly_whenObserverDisposed() {
    sut.observe().subscribeWith(testObserver).assertSubscribed();

    testObserver.dispose();

    verify(sut).dispose();
    testObserver.isDisposed();
  }

  @Test
  public void shouldLogError_whenReceiverUnregisterException() {
    setupNetworkCallbackUnregisterException();

    sut.observe().subscribeWith(testObserver).assertSubscribed();
    testObserver.dispose();

    verify(sut).dispose();
    testObserver.isDisposed();
    verify(sut).onError(anyString(), any(Exception.class));
  }

  @Test
  public void shouldLogError_whenNetworkCallbackUnregisterException() {
    setupForBroadcastReceiverUnregisterException();

    sut.observe().subscribeWith(testObserver).assertSubscribed();
    testObserver.dispose();

    verify(sut).dispose();
    testObserver.isDisposed();
    verify(sut).onError(anyString(), any(Exception.class));
  }

  private void setupForBroadcastReceiverUnregisterException() {
    ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
    doReturn(connectivityManager).when(context).getSystemService(Context.CONNECTIVITY_SERVICE);
    doThrow(Exception.class).when(context).unregisterReceiver(any(BroadcastReceiver.class));
    sut = spy(new MarshmallowNetworkObservingStrategy(context));
  }

  private void setupNetworkCallbackUnregisterException() {
    ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
    doReturn(connectivityManager).when(context).getSystemService(Context.CONNECTIVITY_SERVICE);
    doThrow(Exception.class).when(connectivityManager)
        .unregisterNetworkCallback(any(NetworkCallback.class));

    sut = spy(new MarshmallowNetworkObservingStrategy(context));
  }

  private void setUpDeviceIdleMode(boolean isIdle) {
    context = spy(RuntimeEnvironment.application.getApplicationContext());
    doReturn(isIdle).when(powerManager).isDeviceIdleMode();
    doReturn(powerManager).when(context).getSystemService(POWER_SERVICE);
    sut = spy(new MarshmallowNetworkObservingStrategy(context));
  }

  private ConnectivityManager setUpManagerWithNetworkRequest(@Nullable NetworkRequest networkRequest) {

    ConnectivityManager manager = mock(ConnectivityManager.class);
    doReturn(manager).when(context).getSystemService(CONNECTIVITY_SERVICE);

    sut = spy(networkRequest == null ? new MarshmallowNetworkObservingStrategy(context) : new MarshmallowNetworkObservingStrategy(context,
        networkRequest));

    return manager;
  }
}
