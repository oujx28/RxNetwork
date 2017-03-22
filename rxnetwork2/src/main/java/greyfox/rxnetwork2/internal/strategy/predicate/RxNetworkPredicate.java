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
package greyfox.rxnetwork2.internal.strategy.predicate;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.RxNetwork;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import io.reactivex.functions.Predicate;
import java.util.Arrays;

/**
 * Contains predefined predicates for filtering reactive streams of {@link RxNetwork}.
 *
 * @author Radek Kozak
 */
@RestrictTo(LIBRARY_GROUP)
public final class RxNetworkPredicate {

    @VisibleForTesting(otherwise = PRIVATE)
    RxNetworkPredicate() {
        throw new AssertionError("No instances.");
    }

    /**
     * Predicate that returns true if at least one given {@link NetworkInfo.State state} occurred.
     * <p>
     * This can be useful for filtering reactive streams of {@link RxNetwork}, for example:
     * <pre><code>
     * import static android.net.NetworkInfo.State.CONNECTED;
     * import static android.net.NetworkInfo.State.CONNECTING;
     * import static greyfox.rxnetwork2.internal.strategy.predicate.RxNetworkPredicate.State.hasState;
     *
     * RxNetwork.observe()
     *          .subscribeOn(Schedulers.io())
     *          .filter(hasState(CONNECTED, CONNECTING))
     *          .observeOn(AndroidSchedulers.mainThread())
     *          .subscribe(...);
     * </code></pre>
     *
     * @author Radek Kozak
     * @see NetworkInfo.State
     */
    public static final class State {

        @VisibleForTesting(otherwise = PRIVATE)
        State() {
            throw new AssertionError("No instances.");
        }

        public static Predicate<RxNetworkInfo> hasState(final NetworkInfo.State... networkStates) {
            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    return Arrays.asList(networkStates).contains(networkInfo.getState());
                }
            };
        }
    }

    /**
     * Predicate that returns true if at least one given {@link NetworkInfo#getType} type} occurred.
     * <p>
     * This can be useful for filtering reactive streams of {@link RxNetwork}, for example:
     * <pre><code>
     * import static android.net.ConnectivityManager.TYPE_MOBILE;
     * import static android.net.ConnectivityManager.TYPE_WIFI;
     * import static greyfox.rxnetwork2.internal.strategy.predicate.RxNetworkPredicate.Type.hasType;
     *
     * RxNetwork.observe()
     *          .subscribeOn(Schedulers.io())
     *          .filter(hasType(TYPE_WIFI, TYPE_MOBILE))
     *          .observeOn(AndroidSchedulers.mainThread())
     *          .subscribe(...);
     * </code></pre>
     *
     * @author Radek Kozak
     * @see NetworkInfo.State
     */
    public static final class Type {

        public static final Predicate<RxNetworkInfo> IS_MOBILE = isOfTypeMobile();
        public static final Predicate<RxNetworkInfo> IS_WIFI = isOfTypeWifi();

        @VisibleForTesting(otherwise = PRIVATE)
        Type() {
            throw new AssertionError("No instances.");
        }

        public static Predicate<RxNetworkInfo> hasType(final int... networkTypes) {
            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    for (int type : networkTypes) {
                        if (networkInfo.getType() == type) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

        private static Predicate<RxNetworkInfo> isOfTypeMobile() {
            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    return networkInfo.getType() == TYPE_MOBILE;
                }
            };
        }

        private static Predicate<RxNetworkInfo> isOfTypeWifi() {
            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    return networkInfo.getType() == TYPE_WIFI;
                }
            };
        }
    }

    @RequiresApi(LOLLIPOP)
    public static final class Capabilities {

        @VisibleForTesting(otherwise = PRIVATE)
        Capabilities() {
            throw new AssertionError("No instances.");
        }

        public static Predicate<RxNetworkInfo> hasCapability(final int... capabilities) {

            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    final NetworkCapabilities networkCapabilities
                            = networkInfo.getNetworkCapabilities();

                    if (networkCapabilities != null) {
                        for (Integer capability : capabilities) {
                            if (networkCapabilities.hasCapability(capability)) return true;
                        }
                    }

                    return false;
                }
            };
        }

        public static Predicate<RxNetworkInfo> hasTransport(final int... transportTypes) {
            return new Predicate<RxNetworkInfo>() {
                @Override
                public boolean test(RxNetworkInfo networkInfo) throws Exception {
                    final NetworkCapabilities networkCapabilities
                            = networkInfo.getNetworkCapabilities();

                    if (networkCapabilities != null) {
                        for (Integer transportType : transportTypes) {
                            if (networkCapabilities.hasTransport(transportType)) return true;
                        }
                    }

                    return false;
                }
            };
        }
    }
}