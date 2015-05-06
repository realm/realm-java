/*
 * Copyright 2015 Realm Inc.
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

import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.test.AndroidTestCase;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.android.RealmCursor;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationNameConventions;
import io.realm.entities.Dog;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmCursorTest extends AndroidTestCase {

    private static final int SIZE = 10;
    public static final int NOT_FOUND = -1;

    private Realm realm;
    private RealmCursor cursor;

    private enum CursorGetter {
        STRING, SHORT, INT, LONG, FLOAT, DOUBLE, BLOB;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Realm.deleteRealmFile(getContext());
        realm = Realm.getInstance(getContext());
        populateTestRealm(realm, SIZE);
        cursor = realm.allObjects(AllTypes.class).getCursor();
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
        assertEquals(AllTypes.COL_COUNT, cursor.getColumnCount());
    }

    public void testGetPosition() {
        assertEquals(-1, cursor.getPosition());
        cursor.moveToFirst();
        assertEquals(0, cursor.getPosition());
        cursor.moveToLast();
        assertEquals(9, cursor.getPosition());
    }

    public void testMoveOffsetValid() {
        assertTrue(cursor.move(SIZE / 2));
    }

    public void testMoveOffsetInvalid() {
        assertFalse(cursor.move(SIZE * 2));
        assertFalse(cursor.move(SIZE * -2));
    }

    public void testMoveToPositionCapAtStart() {
        cursor.move(SIZE/2);
        assertFalse(cursor.move(-SIZE));
        assertTrue(cursor.isBeforeFirst());
    }

    public void testMoveToPositionCapAtEnd() {
        cursor.move(SIZE/2);
        assertFalse(cursor.move(SIZE));
        assertTrue(cursor.isAfterLast());
    }

    public void testMoveToPosition() {
        assertTrue(cursor.moveToPosition(SIZE / 2));
        assertEquals(SIZE / 2, cursor.getPosition());
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
        assertEquals(SIZE, cursor.getPosition());
    }

    public void testMoveToPrevious() {
        cursor.moveToLast();
        assertTrue(cursor.moveToPrevious());
        assertEquals(SIZE - 2, cursor.getPosition());
    }

    public void testMoveToPreviousFailed() {
        cursor.moveToFirst();
        assertFalse(cursor.moveToPrevious());
        assertEquals(-1, cursor.getPosition());
    }

    public void testIsFirstYes() {
        cursor.moveToFirst();
        assertTrue(cursor.isFirst());
    }

    public void testIsFirstNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isFirst());
        cursor.move(SIZE * -2);
        assertFalse(cursor.isFirst());
    }

    public void testIsLastYes() {
        cursor.moveToLast();
        assertTrue(cursor.isLast());
    }

    public void testIsLastNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isLast());
        cursor.move(SIZE * 2);
        assertFalse(cursor.isLast());
    }

    public void testBeforeFirstYes() {
        assertTrue(cursor.isBeforeFirst());
    }

    public void testBeforeFirstNo() {
        cursor.moveToFirst();
        assertFalse(cursor.isBeforeFirst());
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
        cursor.setIdColumn("columnLong");
        assertEquals(cursor.getColumnIndex("columnLong"), cursor.getColumnIndex("_id"));
    }

    public void testGetColumnIndexIdColumnNotFound() {
        Cursor c = realm.where(AllTypes.class).findAll().getCursor();
        assertEquals(NOT_FOUND, c.getColumnIndex("_id"));
    }

    public void testGetColumnIndex() {
        assertEquals(1, cursor.getColumnIndex("columnLong"));
    }

    public void testGetColumnIndexNotFound() {
        assertEquals(-1, cursor.getColumnIndex("foo"));
    }

    public void testGetColumnOrThrowIndexIdColumn() {
        cursor.setIdColumn("columnLong");
        assertEquals(cursor.getColumnIndex("columnLong"), cursor.getColumnIndex("_id"));
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
        assertEquals(AllTypes.COL_COUNT, names.length);
        assertEquals("columnString", names[AllTypes.COL_INDEX_STRING]);
        assertEquals("columnRealmList", names[AllTypes.COL_INDEX_LIST]);
    }

    public void testGetColumnCount() {
        assertEquals(9, cursor.getColumnCount());
    }

    // Test that all get<type> method throw IndexOutOfBounds properly
    public void testGetXXXInvalidIndexThrows() {
        cursor.moveToFirst();
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                switch (cursorGetter) {
                    case STRING: cursor.getString(-1); break;
                    case SHORT: cursor.getShort(-1); break;
                    case INT: cursor.getInt(-1); break;
                    case LONG: cursor.getLong(-1); break;
                    case FLOAT: cursor.getFloat(-1); break;
                    case DOUBLE: cursor.getDouble(-1); break;
                    case BLOB: cursor.getBlob(-1); break;
                }
                fail(cursorGetter + " should throw an exception");
            } catch (IndexOutOfBoundsException expected) {
            } catch (Exception wrongException) {
                throw new RuntimeException(cursorGetter + " threw the wrong exception: ", wrongException);
            }
        }
    }

    // Test that all get<type> method throw if field type doesn't match getter type
    public void testGetXXXWrongFieldTypeThrows() {
        cursor.moveToFirst();
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                switch (cursorGetter) {
                    case STRING: cursor.getString(AllTypes.COL_INDEX_LONG); break;
                    case SHORT: cursor.getShort(AllTypes.COL_INDEX_STRING); break;
                    case INT: cursor.getInt(AllTypes.COL_INDEX_STRING); break;
                    case LONG: cursor.getLong(AllTypes.COL_INDEX_STRING); break;
                    case FLOAT: cursor.getFloat(AllTypes.COL_INDEX_STRING); break;
                    case DOUBLE: cursor.getDouble(AllTypes.COL_INDEX_STRING); break;
                    case BLOB: cursor.getBlob(AllTypes.COL_INDEX_STRING); break;
                }
                fail(cursorGetter + " should throw an exception");
            } catch (IllegalArgumentException expected) {
            } catch (Exception wrongException) {
                throw new RuntimeException(cursorGetter + " threw the wrong exception: ", wrongException);
            }
        }
    }

    // Test that all getters fail when the cursor is closed
    public void testGetXXXFailWhenCursorClosed() {
        cursor.close();
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                switch (cursorGetter) {
                    case STRING: cursor.getString(AllTypes.COL_INDEX_LONG); break;
                    case SHORT: cursor.getShort(AllTypes.COL_INDEX_STRING); break;
                    case INT: cursor.getInt(AllTypes.COL_INDEX_STRING); break;
                    case LONG: cursor.getLong(AllTypes.COL_INDEX_STRING); break;
                    case FLOAT: cursor.getFloat(AllTypes.COL_INDEX_STRING); break;
                    case DOUBLE: cursor.getDouble(AllTypes.COL_INDEX_STRING); break;
                    case BLOB: cursor.getBlob(AllTypes.COL_INDEX_STRING); break;
                }
                fail(cursorGetter + " should throw an exception");
            } catch (NullPointerException expected) {
            } catch (Exception wrongException) {
                throw new RuntimeException(cursorGetter + " threw the wrong exception: ", wrongException);
            }
        }
    }

    public void testGetBlob() {
        cursor.moveToFirst();
        byte[] blob = cursor.getBlob(AllTypes.COL_INDEX_BINARY);
        assertArrayEquals(new byte[]{1, 2, 3}, blob);
    }

    public void testGetString() {
        cursor.moveToFirst();
        String str = cursor.getString(AllTypes.COL_INDEX_STRING);
        assertEquals("test data 0", str);
    }

    public void testCopyStringToBufferInvalidIndexThrows() {
        try {
            cursor.moveToFirst();
            cursor.copyStringToBuffer(-1, new CharArrayBuffer(10));
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testCopyStringToBufferInvalidFieldTypeThrows() {
        try {
            cursor.moveToFirst();
            cursor.copyStringToBuffer(AllTypes.COL_INDEX_LONG, new CharArrayBuffer(10));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testCopyStringToBufferNullBufferThrows() {
        try {
            cursor.moveToFirst();
            cursor.copyStringToBuffer(AllTypes.COL_INDEX_STRING, null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testCopyStringToBuffer() {
        String expectedString = "test data 0";
        int expectedLength = expectedString.length();
        cursor.moveToFirst();
        CharArrayBuffer buffer = new CharArrayBuffer(expectedLength);
        cursor.copyStringToBuffer(AllTypes.COL_INDEX_STRING, buffer);
        assertEquals(expectedLength, buffer.sizeCopied);
        assertEquals(expectedLength, buffer.data.length);
        assertEquals("test data 0", new String(buffer.data));
    }

    public void testGetShort() {
        cursor.moveToFirst();
        short value = cursor.getShort(AllTypes.COL_INDEX_LONG);
        assertEquals(0, value);
    }

    public void testGetInt() {
        cursor.moveToFirst();
        int value = cursor.getInt(AllTypes.COL_INDEX_LONG);
        assertEquals(0, value);
    }

    public void testGetLong() {
        cursor.moveToFirst();
        long value = cursor.getLong(AllTypes.COL_INDEX_LONG);
        assertEquals(0, value);
    }

    public void testGetFloat() {
        cursor.moveToFirst();
        float value = cursor.getFloat(AllTypes.COL_INDEX_FLOAT);
        assertEquals(1.234567f, value);
    }

    public void testGetDouble() {
        cursor.moveToFirst();
        double value = cursor.getDouble(AllTypes.COL_INDEX_DOUBLE);
        assertEquals(3.1415d, value);
    }

    public void testGetTypeInvalidIndexThrows() {
        try {
            cursor.getType(-1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testGetType() {
        assertEquals(Cursor.FIELD_TYPE_STRING, cursor.getType(AllTypes.COL_INDEX_STRING));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(AllTypes.COL_INDEX_LONG));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(AllTypes.COL_INDEX_FLOAT));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(AllTypes.COL_INDEX_DOUBLE));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(AllTypes.COL_INDEX_BOOLEAN));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(AllTypes.COL_INDEX_DATE));
        assertEquals(-1, cursor.getType(AllTypes.COL_INDEX_OBJECT));
        assertEquals(-1, cursor.getType(AllTypes.COL_INDEX_LIST));
    }

    public void testIsNullThrows() {
        cursor.moveToFirst();
        try {
            cursor.isNull(AllTypes.COL_INDEX_STRING);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testDeactivateThrows() {
        try {
            cursor.deactivate();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testRequeryThrows() {
        try {
            cursor.requery();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testClose() {
        cursor.close();
        assertTrue(cursor.isClosed());
    }

    public void testRegisterContentObserverThrows() {
        try {
            cursor.registerContentObserver(new CustomContentObserver(new Handler()));
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testUnregisterContentObserverThrows() {
        try {
            cursor.unregisterContentObserver(new CustomContentObserver(new Handler()));
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testRegisterDataSetObserverNullThrows() {
        try {
            cursor.registerDataSetObserver(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterDataSetObserverClosed() {
        final AtomicBoolean success = new AtomicBoolean(false);
        cursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onInvalidated() {
                success.set(true);
            }
        });
        cursor.close();
        assertTrue(success.get());
    }

    public void testRegisterDataSetObserverRealmChanged() {
        RealmResults results = realm.allObjects(AllTypes.class);
        cursor = results.getCursor();
        final AtomicBoolean success = new AtomicBoolean(false);
        cursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                success.set(true);
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        assertTrue(success.get());
    }

    public void testUnregisterDataSetObserver() {
        DataSetObserver observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                fail();
            }
        };
        cursor.registerDataSetObserver(observer);
        cursor.unregisterDataSetObserver(observer);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    public void testSetNofiticationUriThrows() {
        try {
            cursor.setNotificationUri(null, null);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testGetNotificationUriThrows() {
        try {
            cursor.getNotificationUri();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testGetWantsAllOnMoveCalls() {
        assertFalse(cursor.getWantsAllOnMoveCalls());
    }

    public void testGetExtras() {
        assertEquals(Bundle.EMPTY, cursor.getExtras());
    }

    public void testRespond() {
        assertEquals(Bundle.EMPTY, cursor.respond(new Bundle()));
    }

    public void testSetIdAliasFieldNotFoundThrows() {
        try {
            cursor.setIdColumn("foo");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetIdAliasWrongTypeThrows() {
        try {
            cursor.setIdColumn("columnString");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetIdAliasFieldAlreadyExistsThrows() {
        RealmResults<AnnotationNameConventions> result = realm.where(AnnotationNameConventions.class).findAll();
        cursor = result.getCursor();
        try {
            cursor.setIdColumn("id_object");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetIdAlias() {
        cursor.setIdColumn("columnLong");
        assertEquals(cursor.getColumnIndex("_id"), AllTypes.COL_INDEX_LONG);
        cursor.moveToPosition(1);
        assertEquals(1l, cursor.getLong(cursor.getColumnIndex("_id")));
    }

    private class CustomContentObserver extends ContentObserver {
        public CustomContentObserver(Handler handler) {
            super(handler);
        }
    }
}
