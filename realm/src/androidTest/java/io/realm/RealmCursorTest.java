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

import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.android.RealmCursor;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;

public class RealmCursorTest extends AndroidTestCase {

    private Realm realm;
    private RealmCursor cursor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Realm.deleteRealmFile(getContext());
        realm = Realm.getInstance(getContext());
        populateTestRealm(realm, 10);
        cursor = realm.allObjects(AllTypes.class).getCursor();
        cursor.setIdColumn("columnLong");
    }

    private void populateTestRealm(Realm realm, int objects) {
        realm.beginTransaction();
        for (int i = 0; i < objects; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Foo " + i);
            allTypes.setColumnRealmObject(dog);
        }
        realm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (realm != null) {
            realm.close();
        }
    }

    public void testGetCount() {
        assertEquals(10, cursor.getColumnCount());
    }

    public void testGetPosition() {
        assertEquals(-1, cursor.getPosition());
        cursor.moveToFirst();
        assertEquals(0, cursor.getPosition());
        cursor.moveToLast();
        assertEquals(9, cursor.getPosition());
    }

    public void testMoveOffsetValid() {
        assertTrue(cursor.move(5));
    }

    public void testMoveOffsetInvalid() {
        assertFalse(cursor.move(20));
        assertFalse(cursor.move(-20));
    }

    public void testMoveToPositionCapAtStart() {
        cursor.move(5);
        assertFalse(cursor.move(-10));
        assertTrue(cursor.isBeforeFirst());
    }

    public void testMoveToPositionCapAtEnd() {
        cursor.move(5);
        assertFalse(cursor.move(10));
        assertTrue(cursor.isAfterLast());
    }

    public void testMoveToPosition() {
        assertTrue(cursor.moveToPosition(5));
        assertEquals(5, cursor.getPosition());
    }

    public void testMoveToFirst() {
        assertTrue(cursor.moveToFirst());
        assertEquals(0, cursor.getPosition());
    }

    public void testMoveToLast() {
        assertTrue(cursor.moveToLast());
        assertEquals(9, cursor.getPosition());
    }

    public void testMoveToNext() {
        cursor.moveToFirst();
        assertTrue(cursor.moveToNext());
        assertEquals(1, cursor.getPosition());
    }

    public void testMoveToNextFailed() {
        cursor.moveToLast();
        assertFalse(cursor.moveToNext());
        assertEquals(9, cursor.getPosition());
    }

    public void testMoveToPrevious() {
        cursor.moveToLast();
        assertTrue(cursor.moveToPrevious());
        assertEquals(8, cursor.getPosition());
    }

    public void testMoveToPreviousFailed() {
        cursor.moveToFirst();
        assertFalse(cursor.moveToPrevious());
        assertEquals(0, cursor.getPosition());
    }

    public void testIsFirstYes() {
        cursor.moveToFirst();
        assertTrue(cursor.isFirst());
    }

    public void testIsFirstNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isFirst());
        cursor.move(-20);
        assertFalse(cursor.isFirst());
    }

    public void testIsLastYes() {
        cursor.moveToLast();
        assertTrue(cursor.isLast());
    }

    public void testIsLastNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isLast());
        cursor.move(20);
        assertFalse(cursor.isLast());
    }

    public void testBeforeFirstYes() {
        assertTrue(cursor.isBeforeFirst());
    }

    public void testBeforeFirstNo() {
        cursor.moveToFirst();
        assertFalse(cursor.isFirst());
    }

    public void testIsAfterLastYes() {
        cursor.moveToLast();
        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());
    }

    public void testIsAfterLastNo() {
        cursor.moveToLast();
        assertFalse(cursor.isAfterLast());
    }

    public void testGetColumnIndexIdColumn() {
        assertEquals(cursor.getColumnIndex("_id"), cursor.getColumnIndex("columnLong"));
    }

    public void testGetColumnIndexIdColumnNotFound() {
        Cursor c = realm.where(AllTypes.class).findAll().getCursor();
        assertEquals(-1, c.getColumnIndex("_id"));
    }

    public void testGetColumnIndex() {
        assertEquals(1, cursor.getColumnIndex("columnLong"));
    }

    public void testGetColumnIndexNotFound() {
        assertEquals(-1, cursor.getColumnIndex("foo"));
    }

    public void testGetColumnOrThrowIndexIdColumn() {
        assertEquals(cursor.getColumnIndex("_id"), cursor.getColumnIndex("columnLong"));
    }

    public void testGetColumnIndexOrThrowIdColumnNotFoundThrows() {
        Cursor c = realm.where(AllTypes.class).findAll().getCursor();
        try {
            c.getColumnIndexOrThrow("_id");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetColumnIndexOrThrow() {
        assertEquals(1, cursor.getColumnIndexOrThrow("columnLong"));
    }

    public void testGetColumnIndexOrThrowNotFoundThrows() {
        try {
            cursor.getColumnIndexOrThrow("foo");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetColumnNameInvalidIndexThrows() {
        try {
            cursor.getColumnName(-1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testGetColumnName() {
        assertEquals("columnLong", cursor.getColumnName(1));
    }

    public void testGetColumnNames() {
        String[] names = cursor.getColumnNames();
        assertEquals(9, names.length);
        assertEquals("columnString", names[0]);
        assertEquals("columnRealmList", names[8]);
    }

    public void testGetColumnCount() {
        assertEquals(9, cursor.getColumnCount());
    }

    public void testGetBlobInvalidIndexThrows() {
        try {
            cursor.getBlob(-1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testGetBlobInvalidFieldTypeThrows() {
        try {
            cursor.getBlob(1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetBlob() {
        byte[] blob = cursor.getBlob(cursor.getColumnIndex("columnBinary"));
        assertEquals(fail();
    }
//
//    public void testGetStringInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetStringInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetString() {
//        fail();
//    }
//
//    public void testCopyStringToBufferInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testCopyStringToBufferInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testCopyStringToBufferNullBufferThrows() {
//        fail();
//    }
//
//    public void testCopyStringToBuffer() {
//        fail();
//    }
//
//    public void testGetShortInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetShortInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetShort() {
//        fail();
//    }
//
//    public void testGetIntInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetIntInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetInt() {
//        fail();
//    }
//
//    public void testGetLongInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetLongInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetLong() {
//        fail();
//    }
//
//    public void testGetFloatInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetFloatInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetFloat() {
//        fail();
//    }
//
//    public void testGetDoubleInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetDoubleInvalidFieldTypeThrows() {
//        fail();
//    }
//
//    public void testGetDouble() {
//        fail();
//    }
//
//    public void testGetTypeInvalidIndexThrows() {
//        fail();
//    }
//
//    public void testGetType() {
//        fail(); // TODO Test all types
//    }
//
//    public void testIsNullThrows() {
//        fail();
//    }
//
//    public void testDeactivateThrows() {
//        fail();
//    }
//
//    public void testRequeryThrows() {
//        fail();
//    }
//
//    public void testClose() {
//        fail(); // TODO and isClose
//    }
//
//    public void testMethodsFailWhenCursorClosed() {
//        fail(); // Test all methods fail when cursor is closed
//    }
//
//    public void testRegisterContentObserverThrows() {
//        fail();
//    }
//
//    public void testUnregisterContentObserverThrows() {
//        fail();
//    }
//
//    public void testRegisterDataSetObserverClosed() {
//        fail(); // TODO only works with close()
//    }
//
//    public void testRegisterDataSetObserverRealmChanged() {
//        fail(); // TODO only works with close()
//    }
//
//    public void testUnregisterDataSetObserver() {
//        fail(); // TODO only works with close()
//    }
//
//    public void testSetNofiticationUriThrows() {
//        fail();
//    }
//
//    public void testGetNotificationUriThrows() {
//        fail();
//    }
//
//    public void testGetWantsAllOnMoveCalls() {
//        fail();
//    }
//
//    public void testGetExtras() {
//        fail();
//    }
//
//    public void testRespond() {
//        fail();
//    }
//
//    public void testSetIdAliasFieldNotFoundThrows() {
//        fail();
//    }
//
//    public void testSetIdAliasWrongTypeThrows() {
//        fail();
//    }
//
//    public void testSetIdAliasFieldAlreadyExistsThrows() {
//        fail();
//    }
//
//    public void testSetIdAlias() {
//        fail();
//    }
}
