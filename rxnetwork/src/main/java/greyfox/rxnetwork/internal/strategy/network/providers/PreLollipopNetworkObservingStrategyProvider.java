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
package greyfox.rxnetwork.internal.strategy.network.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.impl.PreLollipopNetworkObservingStrategy;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork.internal.os.Build.isLessThanLollipop;

/**
 * Provides {@link NetworkObservingStrategy} implementation for pre-Lollipop devices.
 *
 * @author Radek Kozak
 */
final class PreLollipopNetworkObservingStrategyProvider
    implements NetworkObservingStrategyProvider {

  private final Context context;

  PreLollipopNetworkObservingStrategyProvider(@NonNull Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override
  public boolean canProvide() {
    return isLessThanLollipop();
  }

  @Override
  public NetworkObservingStrategy provide() {
    return new PreLollipopNetworkObservingStrategy(context);
  }
}
