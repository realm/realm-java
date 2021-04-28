/*
 * Copyright 2020 Realm Inc.
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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.entities.StringOnly;

@RunWith(AndroidJUnit4.class)
public class UTFStringsTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void valid_utf() {
        realm.beginTransaction();

        StringOnly validString = new StringOnly();
        validString.setChars("\uD800\uDC00");
        realm.copyToRealm(validString);

        realm.commitTransaction();
    }

    @Test
    public void invalid_first_half() {
        realm.beginTransaction();

        // Test invalid first surrogate
        StringOnly invalidFirstSurrogate = new StringOnly();
        invalidFirstSurrogate.setChars("\uDC00\uD800");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal Argument: Failure when converting to UTF-8: Invalid first half of surrogate pair; error_code = 5;  0xdc00 0xd800");

        realm.copyToRealm(invalidFirstSurrogate);
        realm.commitTransaction();
    }

    @Test
    public void invalid_second_half() {
        realm.beginTransaction();

        // Test invalid second surrogate
        StringOnly invalidSecondSurrogate = new StringOnly();
        invalidSecondSurrogate.setChars("\uD800\uD800");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal Argument: Failure when converting to UTF-8: Invalid second half of surrogate pair; error_code = 7;  0xd800 0xd800");

        realm.copyToRealm(invalidSecondSurrogate);

        realm.commitTransaction();
    }

    @Test
    public void incomplete_surrogate() {
        realm.beginTransaction();

        // Test incomplete surrogate
        StringOnly incompleteSurrogate = new StringOnly();
        incompleteSurrogate.setChars("\u0000\uD800");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal Argument: Failure when converting to UTF-8: Incomplete surrogate pair; error_code = 6;  0x0000 0xd800");

        realm.copyToRealm(incompleteSurrogate);

        realm.commitTransaction();
    }
}
