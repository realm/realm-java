/*
 * Copyright 2014 Realm Inc.
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

package io.realm.internal;

import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(Parameterized.class)
public class JNITableInsertTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;

    private List<Object> value = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        List<Object> value = new ArrayList<>();
        value.add(0, true);
        value.add(1, "abc");
        value.add(2, 123L);
        value.add(3, 987.123f);
        value.add(4, 1234567.898d);
        value.add(5, new Date(645342));
        value.add(6, new byte[]{1, 2, 3, 4, 5});
        return Arrays.asList(
                new Object[]{value},
                new Object[]{value}
        );
    }

    public JNITableInsertTest(List<Object> value) {
        this.value = value;
    }

    @Test
    public void testGenericAddOnTable() {
        for (int i = 0; i < value.size(); i++) {
            for (int j = 0; j < value.size(); j++) {
                final Object valueI = value.get(i);
                final Object valueJ = value.get(j);

                TestHelper.createTable(sharedRealm, "temp" + i + "_" + j, new TestHelper.AdditionalTableSetup() {
                    @Override
                    public void execute(Table t) {
                        // If the objects matches no exception will be thrown.
                        if (valueI.getClass().equals(valueJ.getClass())) {
                            assertTrue(true);
                        } else {
                            // Adds column.
                            t.addColumn(TestHelper.getColumnType(valueJ), valueJ.getClass().getSimpleName());
                            // Adds value.
                            try {
                                TestHelper.addRowWithValues(t, valueI);
                                fail("No matching type");
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                });
            }
        }
    }

}

