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
package greyfox.rxnetwork.internal.os;

import greyfox.rxnetwork.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static greyfox.rxnetwork.internal.os.Build.isAtLeastLollipop;
import static greyfox.rxnetwork.internal.os.Build.isAtLeastMarshmallow;
import static greyfox.rxnetwork.internal.os.Build.isLessThanLollipop;
import static greyfox.rxnetwork.internal.os.Build.isLessThanMarshmallow;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BuildTest {

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new Build();
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldBe_atLeastLollipop() {
    assertThat(isAtLeastLollipop()).isTrue();
  }

  @Test
  @Config(sdk = M)
  public void shouldBe_atLeastMarshmallow() {
    assertThat(isAtLeastMarshmallow()).isTrue();
  }

  @Test
  @Config(sdk = KITKAT)
  public void shouldBe_lessThanLollipop() {
    assertThat(isLessThanLollipop()).isTrue();
  }

  @Test
  @Config(sdk = LOLLIPOP_MR1)
  public void shouldBe_lessThanMarshmallow() {
    assertThat(isLessThanMarshmallow()).isTrue();
  }
}
