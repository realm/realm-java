/*
 * Copyright 2016 Realm Inc.
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

package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UserTests {

    @Test
    public void toAndFromJson() {
        User user1 = createTestUser();
        User user2 = User.fromJson(user1.toJson());
        assertEquals(user1, user2);
    }
}
