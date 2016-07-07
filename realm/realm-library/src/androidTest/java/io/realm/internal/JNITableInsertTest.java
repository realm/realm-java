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

import android.test.MoreAsserts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.TestHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(Parameterized.class)
public class JNITableInsertTest {

    List<Object> value = new ArrayList<Object>();

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        List<Object> value = new ArrayList<Object>();
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
    public void testShouldThrowExceptionWhenColumnNameIsTooLong() {

        Table table = new Table();
        try {
            table.addColumn(RealmFieldType.STRING, "THIS STRING HAS 64 CHARACTERS, "
                    + "LONGER THAN THE MAX 63 CHARACTERS");
            fail("Too long name");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testWhenColumnNameIsExactly63CharLong() {

        Table table = new Table();
        table.addColumn(RealmFieldType.STRING, "THIS STRING HAS 63 CHARACTERS PERFECT FOR THE MAX 63 CHARACTERS");
    }

    @Test
    public void testGenericAddOnTable() {
        for (int i = 0; i < value.size(); i++) {
            for (int j = 0; j < value.size(); j++) {

                Table t = new Table();

                //If the objects matches no exception will be thrown
                if (value.get(i).getClass().equals(value.get(j).getClass())) {
                    assertTrue(true);

                } else {
                    //Add column
                    t.addColumn(TestHelper.getColumnType(value.get(j)), value.get(j).getClass().getSimpleName());
                    //Add value
                    try {
                        t.add(value.get(i));
                        fail("No matching type");
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
    }

}

