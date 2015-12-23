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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.internal.test.MixedData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class JNIMixedTypeTest {

    List<MixedData> mixedDataList = new ArrayList<MixedData>();

    @Parameterized.Parameters
    public static List<Object[]> parameters() {
        //Adding MixedData to the list
        List<MixedData> mixedDataList = new ArrayList<MixedData>();
        mixedDataList.add(0, new MixedData(RealmFieldType.INTEGER, 123L));
        mixedDataList.add(1, new MixedData(RealmFieldType.FLOAT, 987.123f));
        mixedDataList.add(2, new MixedData(RealmFieldType.DOUBLE, 1234567.898d));
        mixedDataList.add(3, new MixedData(RealmFieldType.BOOLEAN, true));
        mixedDataList.add(4, new MixedData(RealmFieldType.STRING, "abc"));
        mixedDataList.add(5, new MixedData(RealmFieldType.BINARY, new byte[]{1, 2, 3, 4, 5}));
        mixedDataList.add(6, new MixedData(RealmFieldType.DATE, new Date(645342)));

        return Arrays.asList(
                new Object[]{mixedDataList},
                new Object[]{mixedDataList},
                new Object[]{mixedDataList},
                new Object[]{mixedDataList}
        );
    }

    public JNIMixedTypeTest(List<MixedData> mixedDataList) {
        this.mixedDataList = mixedDataList;
    }

    @Test
    public void testShouldMatchMixedValues() {
        for (int i = 0; i < mixedDataList.size(); i++) {
            for (int j = 0; j < mixedDataList.size(); j++) {
                if (mixedDataList.get(i).value == mixedDataList.get(j).value) {
                    assertEquals(mixedDataList.get(i).value, mixedDataList.get(j).value);

                } else {
                    assertNotSame(mixedDataList.get(i).value, mixedDataList.get(j).value);

                }
            }
        }
    }

    @Test
    public void testShouldFailOnWrongTypeRetrieval() {
        for (int i = 0; i < mixedDataList.size(); i++) {
            Object value = mixedDataList.get(i).type != RealmFieldType.STRING ? "abc" : 123;
            Mixed mixed = Mixed.mixedValue(value);

            switch (mixedDataList.get(i).type) {
                case BINARY:
                    try {
                        mixed.getBinaryByteArray();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case DATE:
                    try {
                        mixed.getDateValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case BOOLEAN:
                    try {
                        mixed.getBooleanValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case INTEGER:
                    try {
                        mixed.getLongValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case FLOAT:
                    try {
                        mixed.getFloatValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case DOUBLE:
                    try {
                        mixed.getDoubleValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                case STRING:
                    try {
                        mixed.getStringValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException ignored) {
                    }
                    break;
                default:
                    fail("wrong type");
                    break;
            }
        }
    }
}

