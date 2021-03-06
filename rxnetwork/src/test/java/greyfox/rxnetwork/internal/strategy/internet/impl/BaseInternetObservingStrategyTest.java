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
package greyfox.rxnetwork.internal.strategy.internet.impl;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(Parameterized.class)
public class BaseInternetObservingStrategyTest {

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Parameterized.Parameter public int invalidPort;

  @Parameterized.Parameters
  public static Iterable<? extends Integer> invalidPorts() {
    return Arrays.asList(-1, 0, 65536);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrow_whenTryingToBuildStrategyWithInvalidPort() {
    SocketInternetObservingStrategy.builder().port(invalidPort);
  }
}
