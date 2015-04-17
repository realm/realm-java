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

package io.realm;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.dynamic.DynamicRealmList;
import io.realm.dynamic.DynamicRealmObject;
import io.realm.entities.AllJavaTypes;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class DynamicRealmObjectTest extends AndroidTestCase {

    private Realm realm;
    private DynamicRealmObject dObj;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        Realm.setSchema(AllJavaTypes.class);
        realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        obj.setColumnString("str");
        obj.setColumnShort((short) 1);
        obj.setColumnInt(1);
        obj.setColumnLong(1);
        obj.setColumnFloat(1.23f);
        obj.setColumnDouble(1.234d);
        obj.setColumnBinary(new byte[]{1, 2, 3});
        obj.setColumnBoolean(true);
        obj.setColumnDate(new Date(1000));
        obj.setColumnObject(obj);
        obj.getColumnList().add(obj);
        dObj = new DynamicRealmObject(realm, obj.row);
        realm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        realm.close();
        Realm.setSchema(null);
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetBooleanIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getBoolean(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetBoolean() {
        assertTrue(dObj.getBoolean("columnBoolean"));
    }

    public void testGetLinkedBoolean() {
        try {
            dObj.getBoolean("columnObject.columnBoolean");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetShortIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getShort(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetShort() {
        assertEquals(1, dObj.getShort("columnShort"));
    }

    public void testGetLinkedShort() {
        try {
            dObj.getShort("columnObject.columnShort");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetIntIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getInt(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetInt() {
        assertEquals(1, dObj.getInt("columnInt"));
    }

    public void testGetLinkedInt() {
        try {
            dObj.getInt("columnObject.columnInt");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetLongIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getLong(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetLong() {
        assertEquals(1, dObj.getLong("columnLong"));
    }

    public void testGetLinkedLong() {
        try {
            dObj.getLong("columnObject.columnLong");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetFloatIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getFloat(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetFloat() {
        assertEquals(1.23f, dObj.getFloat("columnFloat"));
    }

    public void testGetLinkedFloat() {
        try {
            dObj.getFloat("columnObject.columnFloat");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetDoubleIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getFloat(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetDouble() {
        assertEquals(1.234d, dObj.getDouble("columnDouble"));
    }

    public void testGetLinkedDouble() {
        try {
            dObj.getDouble("columnObject.columnDouble");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetBytesIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getBytes(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetBytes() {
        assertArrayEquals(new byte[]{1, 2, 3}, dObj.getBytes("columnBinary"));
    }

    public void testGetLinkedBytes() {
        try {
            dObj.getDouble("columnObject.columnBinary");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetDateIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getDate(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetDate() {
        assertEquals(new Date(1000), dObj.getDate("columnDate"));
    }

    public void testGetLinkedDate() {
        try {
            dObj.getDate("columnObject.columnDate");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetStringIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnInt");
        for (String argument : arguments) {
            try {
                dObj.getString(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetString() {
        assertEquals("str", dObj.getString("columnString"));
    }

    public void testGetLinkedString() {
        try {
            dObj.getDate("columnObject.columnString");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test invalid input: <empty, non-existing field, wrong field type>
    public void testGetObjectIllegalArguments() {
        List<String> arguments = Arrays.asList(null, "foo", "columnString");
        for (String argument : arguments) {
            try {
                dObj.getRealmObject(argument);
                fail(argument + " should throw exception.");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testGetObject() {
        assertEquals(dObj, dObj.getRealmObject("columnObject"));
    }

    public void testGetLinkedObject() {
        try {
            dObj.getRealmObject("columnObject.columnObject");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetList() {
        DynamicRealmList list = dObj.getRealmList("columnList");
        assertEquals(1, list.size());
        assertEquals(dObj, list.get(0));
    }

    public void testGetKeys() {
        String[] keys = dObj.getKeys();
        fail();

    }

    public void testEquals() {
        fail();
    }

    public void testHashcode() {
        fail();
    }

    public void testToString() {
        fail();
    }
}
