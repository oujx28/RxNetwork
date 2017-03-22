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
package greyfox.rxnetwork2.internal.strategy.impl;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import android.util.Log;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.net.RxNetworkInfoHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * RxNetworkInfo observing strategy for Android devices with API 21 (Lollipop) or higher.
 *
 * @author Radek Kozak
 */
@TargetApi(LOLLIPOP)
public class LollipopNetworkObservingStrategy extends BuiltInNetworkObservingStrategy {

    private static final String TAG = LollipopNetworkObservingStrategy.class.getSimpleName();
    private final ConnectivityManager manager;
    private NetworkCallback networkCallback;

    public LollipopNetworkObservingStrategy(@NonNull Context context) {
        manager = (ConnectivityManager) checkNotNull(context, "context == null")
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public Observable<RxNetworkInfo> observe() {
        return Observable.create(new LollipopOnSubscribe())
                .defaultIfEmpty(RxNetworkInfo.create())
                .distinctUntilChanged();
    }

    private void register() {
        manager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
    }

    @Override
    void dispose() {
        try {
            manager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't unregister network callback: " + e.getMessage());
        }
    }

    private final class LollipopOnSubscribe implements ObservableOnSubscribe<RxNetworkInfo> {

        @Override
        public void subscribe(final ObservableEmitter<RxNetworkInfo> emitter) throws Exception {
            networkCallback = new NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    emitter.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
                }

                @Override
                public void onLost(Network network) {
                    emitter.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
                }
            };
            emitter.setCancellable(new StrategyCancellable());
            register();
        }
    }
}
