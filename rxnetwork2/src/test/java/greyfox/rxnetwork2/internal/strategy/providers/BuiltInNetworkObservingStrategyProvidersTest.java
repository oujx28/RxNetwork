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
package greyfox.rxnetwork2.internal.strategy.providers;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import greyfox.rxnetwork2.BuildConfig;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyProvider;
import java.util.Collection;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BuiltInNetworkObservingStrategyProvidersTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @Mock NetworkObservingStrategyProvider ANY_PROVIDER;

    Context context = RuntimeEnvironment.application;

    Collection<NetworkObservingStrategyProvider> sut
            = BuiltInNetworkObservingStrategyProviders.get(context);

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new BuiltInNetworkObservingStrategyProviders();
    }

    @Test
    public void builtInProviders_shouldNeverBeNull() {
        assertThat(sut).isNotNull();
    }

    @Test
    public void builtInProviders_shouldHaveAtLeastOneProvider() {
        assertThat(sut).hasAtLeastOneElementOfType(NetworkObservingStrategyProvider.class);
    }

    @Test
    @Config(minSdk = KITKAT_WATCH)
    public void onlyOneProviderShouldBeAbleToProvide_forGivenPlatform() {
        assertThat(sut).haveExactly(1, new CanProvide());
    }

    @Test
    public void builtInProviders_shouldNotHaveDuplicates() {
        assertThat(sut).doesNotHaveDuplicates();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void builtInProviders_shouldBeUnmodifiable() {
        // hacky but we don't want to test for every collection methods
        assertThat(sut.getClass().getName()).contains("Unmodifiable");
        sut.add(ANY_PROVIDER);
    }

    static class CanProvide extends Condition<NetworkObservingStrategyProvider> {

        @Override
        public boolean matches(NetworkObservingStrategyProvider value) {
            return value.canProvide();
        }
    }
}