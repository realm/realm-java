/*
 * Copyright 2017 Realm Inc.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.realm.entities.NullTypes;
import io.realm.internal.Table;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.ManagedRealmListForValueTests.ListType.BINARY_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.BOOLEAN_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.BYTE_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.DATE_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.DOUBLE_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.FLOAT_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.INTEGER_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.LONG_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.SHORT_LIST;
import static io.realm.ManagedRealmListForValueTests.ListType.STRING_LIST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Unit tests specific for RealmList with value elements.
 */
@RunWith(Parameterized.class)
public class ManagedRealmListForValueTests extends CollectionTests {

    static final int NON_NULL_TEST_SIZE = 10;
    static final int NULLABLE_TEST_SIZE = NON_NULL_TEST_SIZE * 2;

    enum ListType {
        STRING_LIST(String.class.getName()),
        BOOLEAN_LIST(Boolean.class.getName()),
        BINARY_LIST(byte[].class.getSimpleName()/* using simple name since array class is a bit special */),
        LONG_LIST(Long.class.getName()),
        INTEGER_LIST(Integer.class.getName()),
        SHORT_LIST(Short.class.getName()),
        BYTE_LIST(Byte.class.getName()),
        DOUBLE_LIST(Double.class.getName()),
        FLOAT_LIST(Float.class.getName()),
        DATE_LIST(Date.class.getName());

        private final String valueTypeName;

        ListType(String valueTypeName) {
            this.valueTypeName = valueTypeName;
        }

        public String getValueTypeName() {
            return valueTypeName;
        }
    }

    @Parameterized.Parameters(name = "{index}: Type: {0}, Nullable?: {1}")
    public static Collection<Object[]> parameters() {
        final List<Object[]> paramsList = new ArrayList<>();
        for (ListType listType : ListType.values()) {
            paramsList.add(new Object[] {listType, Boolean.TRUE});
            paramsList.add(new Object[] {listType, Boolean.FALSE});
        }
        return paramsList;
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Parameterized.Parameter
    public ListType listType;

    @Parameterized.Parameter(1)
    public Boolean isTypeNullable;

    private Realm realm;
    private NullTypes object;
    private RealmList list;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        final RealmConfiguration.Builder configurationBuilder = configFactory.createConfigurationBuilder();
        configurationBuilder.schema(NullTypes.class);
        RealmConfiguration realmConfig = configurationBuilder.build();

        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        object = realm.createObject(NullTypes.class, 0);
        for (ListType type : ListType.values()) {
            for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
                switch (type) {
                    case STRING_LIST: {
                        final RealmList nonnull = object.getFieldStringListNotNull();
                        nonnull.add(generateValue(STRING_LIST, i));
                        final RealmList nullable = object.getFieldStringListNull();
                        nullable.add("" + i);
                        nullable.add(null);
                    }
                    break;
                    case BOOLEAN_LIST: {
                        final RealmList nonnull = object.getFieldBooleanListNotNull();
                        nonnull.add(generateValue(BOOLEAN_LIST, i));
                        final RealmList nullable = object.getFieldBooleanListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case BINARY_LIST: {
                        final RealmList nonnull = object.getFieldBinaryListNotNull();
                        nonnull.add(generateValue(BINARY_LIST, i));
                        final RealmList nullable = object.getFieldBinaryListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case LONG_LIST: {
                        final RealmList nonnull = object.getFieldLongListNotNull();
                        nonnull.add(generateValue(LONG_LIST, i));
                        final RealmList nullable = object.getFieldLongListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case INTEGER_LIST: {
                        final RealmList nonnull = object.getFieldIntegerListNotNull();
                        nonnull.add(generateValue(INTEGER_LIST, i));
                        final RealmList nullable = object.getFieldIntegerListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case SHORT_LIST: {
                        final RealmList nonnull = object.getFieldShortListNotNull();
                        nonnull.add(generateValue(SHORT_LIST, i));
                        final RealmList nullable = object.getFieldShortListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case BYTE_LIST: {
                        final RealmList nonnull = object.getFieldByteListNotNull();
                        nonnull.add(generateValue(BYTE_LIST, i));
                        final RealmList nullable = object.getFieldByteListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case DOUBLE_LIST: {
                        final RealmList nonnull = object.getFieldDoubleListNotNull();
                        nonnull.add(generateValue(DOUBLE_LIST, i));
                        final RealmList nullable = object.getFieldDoubleListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case FLOAT_LIST: {
                        final RealmList nonnull = object.getFieldFloatListNotNull();
                        nonnull.add(generateValue(FLOAT_LIST, i));
                        final RealmList nullable = object.getFieldFloatListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    case DATE_LIST: {
                        final RealmList nonnull = object.getFieldDateListNotNull();
                        nonnull.add(generateValue(DATE_LIST, i));
                        final RealmList nullable = object.getFieldDateListNull();
                        nullable.add(nonnull.last());
                        nullable.add(null);
                    }
                    break;
                    default:
                        throw new AssertionError("unexpected value type: " + listType.name());
                }
            }
        }
        realm.commitTransaction();

        list = getListFor(object, listType, isTypeNullable);
    }

    static RealmList<?> getListFor(NullTypes object, ListType listType, boolean nullable) {
        switch (listType) {
            case STRING_LIST:
                return nullable ? object.getFieldStringListNull() : object.getFieldStringListNotNull();
            case BOOLEAN_LIST:
                return nullable ? object.getFieldBooleanListNull() : object.getFieldBooleanListNotNull();
            case BINARY_LIST:
                return nullable ? object.getFieldBinaryListNull() : object.getFieldBinaryListNotNull();
            case LONG_LIST:
                return nullable ? object.getFieldLongListNull() : object.getFieldLongListNotNull();
            case INTEGER_LIST:
                return nullable ? object.getFieldIntegerListNull() : object.getFieldIntegerListNotNull();
            case SHORT_LIST:
                return nullable ? object.getFieldShortListNull() : object.getFieldShortListNotNull();
            case BYTE_LIST:
                return nullable ? object.getFieldByteListNull() : object.getFieldByteListNotNull();
            case DOUBLE_LIST:
                return nullable ? object.getFieldDoubleListNull() : object.getFieldDoubleListNotNull();
            case FLOAT_LIST:
                return nullable ? object.getFieldFloatListNull() : object.getFieldFloatListNotNull();
            case DATE_LIST:
                return nullable ? object.getFieldDateListNull() : object.getFieldDateListNotNull();
            default:
                throw new AssertionError("unexpected value type: " + listType.name());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    static Object generateValue(ListType listType, int i) {
        switch (listType) {
            case STRING_LIST:
                return "" + i;
            case BOOLEAN_LIST:
                return i % 2 == 0 ? Boolean.FALSE : Boolean.TRUE;
            case BINARY_LIST:
                return new byte[] {(byte) i};
            case LONG_LIST:
                return (long) i;
            case INTEGER_LIST:
                return i;
            case SHORT_LIST:
                return (short) i;
            case BYTE_LIST:
                return (byte) i;
            case DOUBLE_LIST:
                return (double) i;
            case FLOAT_LIST:
                return (float) i;
            case DATE_LIST:
                return new Date(i);
            default:
                throw new AssertionError("unexpected value type: " + listType.name());
        }
    }

    private static Object generateHugeValue(ListType listType, int size) {
        final byte[] bytes = new byte[size];
        switch (listType) {
            case STRING_LIST:
                Arrays.fill(bytes, (byte) 'a');
                return new String(bytes, Charset.forName("US-ASCII"));
            case BINARY_LIST:
                return bytes;
            default:
                throw new AssertionError("'generateHugeValue' does not support this type: " + listType.name());
        }
    }

    private void assertValueEquals(@Nullable Object expected, @Nullable Object actual) {
        assertValueEquals(null, expected, actual);
    }

    private void assertValueEquals(@SuppressWarnings("SameParameterValue") @Nullable String message, @Nullable Object expected, @Nullable Object actual) {
        if (listType == BINARY_LIST) {
            assertArrayEquals(message, (byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(message, expected, actual);
        }
    }

    @Test
    public void readValues() {
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            switch (listType) {
                case STRING_LIST: {
                    assertEquals(generateValue(STRING_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case BOOLEAN_LIST: {
                    assertEquals(generateValue(BOOLEAN_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case BINARY_LIST: {
                    assertArrayEquals((byte[]) generateValue(BINARY_LIST, i), (byte[]) list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case LONG_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(LONG_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case INTEGER_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(INTEGER_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case SHORT_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(SHORT_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case BYTE_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(BYTE_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case DOUBLE_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(DOUBLE_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case FLOAT_LIST: {
                    //noinspection UnnecessaryBoxing
                    assertEquals(generateValue(FLOAT_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                case DATE_LIST: {
                    assertEquals(generateValue(DATE_LIST, i), list.get(isTypeNullable ? (i * 2) : i));
                }
                break;
                default:
                    throw new AssertionError("unexpected value type: " + listType.name());
            }
            if (isTypeNullable) {
                assertNull(list.get(i * 2 + 1));
            }
        }
    }

    @Test
    public void isValid() {
        assertTrue(list.isValid());

        realm.close();

        assertFalse(list.isValid());
    }

    @Test
    public void isValid_whenParentRemoved() {
        realm.beginTransaction();
        object.deleteFromRealm();
        realm.commitTransaction();

        // RealmList contained in removed object is invalid.
        assertFalse(list.isValid());
    }

    @Test
    public void add_exceedingSizeLimitValueThrows() {
        if (listType != STRING_LIST && listType != BINARY_LIST) {
            return;
        }

        final int sizeLimit;
        switch (listType) {
            case STRING_LIST:
                sizeLimit = Table.MAX_STRING_SIZE;
                break;
            case BINARY_LIST:
                sizeLimit = Table.MAX_BINARY_SIZE;
                break;
            default:
                throw new AssertionError("Unexpected list type: " + listType.name());
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //noinspection unchecked
                list.add(generateHugeValue(listType, sizeLimit));
            }
        });

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final long sizeBeforeException = list.size();
                thrown.expect(IllegalArgumentException.class);
                try {
                    //noinspection unchecked
                    list.add(generateHugeValue(listType, sizeLimit + 1));
                } finally {
                    // FIXME This assertion fails now. Code will be fixed in master branch first.
                    assertEquals(sizeBeforeException, list.size());
                }
            }
        });
    }

    @Test
    public void move_outOfBoundsLowerThrows() {
        realm.beginTransaction();
        try {
            list.move(0, -1);
            fail("Indexes < 0 should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void move_outOfBoundsHigherThrows() {
        realm.beginTransaction();
        try {
            list.move(list.size() - 1, list.size());
            fail("Indexes >= size() should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
            ignored.printStackTrace();
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void clear_then_add() {
        realm.beginTransaction();
        list.clear();

        assertTrue(list.isEmpty());

        //noinspection unchecked
        list.add(generateValue(listType, -100));

        realm.commitTransaction();

        assertEquals(1, list.size());
    }

    @Test
    public void size() {
        assertEquals(isTypeNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE, list.size());
    }

    @Test
    public void remove_nonNullByIndex() {
        final int targetIndex = 6;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Object removed = list.remove(targetIndex);
                final int dataIndex = isTypeNullable ? targetIndex / 2 : targetIndex;
                assertValueEquals(generateValue(listType, dataIndex), removed);
            }
        });

        assertEquals(isTypeNullable ? (NULLABLE_TEST_SIZE - 1) : (NON_NULL_TEST_SIZE - 1), list.size());
        for (int i = 0; i < list.size(); i++) {
            final int originalIndex = i < targetIndex ? i : i + 1;
            if (isTypeNullable) {
                if (originalIndex % 2 == 1) {
                    assertNull(list.get(i));
                } else {
                    assertValueEquals(generateValue(listType, originalIndex / 2), list.get(i));
                }
            } else {
                assertValueEquals(generateValue(listType, originalIndex), list.get(i));
            }
        }
    }

    @Test
    public void remove_nullByIndex() {
        if (!isTypeNullable) {
            return;
        }

        final int targetIndex = 7;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                assertNull(list.remove(targetIndex));
                assertEquals(NULLABLE_TEST_SIZE - 1, list.size());
            }
        });

        for (int i = 0; i < list.size(); i++) {
            final int originalIndex = i < targetIndex ? i : i + 1;
            if (originalIndex % 2 == 1) {
                assertNull(list.get(i));
            } else {
                assertValueEquals(generateValue(listType, originalIndex / 2), list.get(i));
            }
        }
    }

    @Test
    public void remove_first() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Object removed = list.remove(0);
                assertValueEquals(generateValue(listType, 0), removed);
            }
        });

        assertEquals((isTypeNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE) - 1, list.size());
    }

    @Test
    public void remove_last() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Object removed = list.remove((isTypeNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE) - 1);
                if (isTypeNullable) {
                    assertNull(removed);
                } else {
                    assertValueEquals(generateValue(listType, NON_NULL_TEST_SIZE - 1), removed);
                }
            }
        });

        assertEquals((isTypeNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE) - 1, list.size());
    }

    @Test
    public void remove_fromEmptyListThrows() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                list.clear();
                thrown.expect(IndexOutOfBoundsException.class);
                list.remove(0);
            }
        });
    }

    @Test
    public void remove_byObject() {
        final Object value = list.get(0);
        final int initialSize = list.size();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (listType == BINARY_LIST) {
                    assertFalse(list.remove(value));  // since 'equals()' never return true against binary array.
                } else {
                    assertTrue(list.remove(value));
                }
            }
        });

        assertEquals((listType == BINARY_LIST) ? initialSize : (initialSize - 1), list.size());
    }

    @Test
    public void remove_byNull() {
        final int initialSize = list.size();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (isTypeNullable) {
                    assertTrue(list.remove(null));
                } else {
                    assertFalse(list.remove(null));
                }
            }
        });

        assertEquals(isTypeNullable ? (initialSize - 1) : initialSize, list.size());
    }

    @Test
    public void deleteFirst() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                assertTrue(list.deleteFirstFromRealm());
            }
        });

        assertEquals(isTypeNullable ? (NULLABLE_TEST_SIZE - 1) : (NON_NULL_TEST_SIZE - 1), list.size());
        for (int i = 0; i < list.size(); i++) {
            final int originalIndex = i + 1;
            if (isTypeNullable) {
                if (originalIndex % 2 == 1) {
                    assertNull(list.get(i));
                } else {
                    assertValueEquals(generateValue(listType, originalIndex / 2), list.get(i));
                }
            } else {
                assertValueEquals(generateValue(listType, originalIndex), list.get(i));
            }
        }
    }

    @Test
    public void deleteLast() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                assertTrue(list.deleteLastFromRealm());
            }
        });

        assertEquals(isTypeNullable ? (NULLABLE_TEST_SIZE - 1) : (NON_NULL_TEST_SIZE - 1), list.size());
        for (int i = 0; i < list.size(); i++) {
            if (isTypeNullable) {
                if (i % 2 == 1) {
                    assertNull(list.get(i));
                } else {
                    assertValueEquals(generateValue(listType, i / 2), list.get(i));
                }
            } else {
                assertValueEquals(generateValue(listType, i), list.get(i));
            }
        }
    }

    @Test
    public void addAt_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        //noinspection unchecked
        list.add(generateValue(listType, 100));
    }

    @Test
    public void addAt_invalidIndex() {
        final int initialSize = list.size();
        try {
            realm.beginTransaction();
            //noinspection unchecked
            list.add(initialSize + 1, generateValue(listType, 1000));
            fail();
        } catch (IndexOutOfBoundsException e) {
            // make sure that the size is not changed
            assertEquals(initialSize, list.size());
        }
    }

    @Test
    public void set_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        //noinspection unchecked
        list.set(0, generateValue(listType, 100));
    }

    @Test
    public void set_invalidIndex() {
        final int initialSize = list.size();
        try {
            realm.beginTransaction();
            //noinspection unchecked
            list.set(initialSize, generateValue(listType, 1000));
            fail();
        } catch (IndexOutOfBoundsException e) {
            // make sure that the size is not changed
            assertEquals(initialSize, list.size());
        }
    }

    @Test
    public void move_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.move(0, 1);
    }

    @Test
    public void clear_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.clear();
    }

    @Test
    public void remove_atAfterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.remove(0);
    }

    @Test
    public void remove_objectAfterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.remove(generateValue(listType, 4));
    }

    @Test
    public void remove_unsupportedTypeIgnored() {
        final int initialSize = list.size();

        final List<Object> unsupportedValues = Arrays.<Object>asList(
                new int[] {0},
                new StringBuilder("0")
        );
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Object unsupportedValue : unsupportedValues) {
                    //noinspection UseBulkOperation
                    assertFalse(list.remove(unsupportedValue));
                }
            }
        });

        assertEquals(initialSize, list.size());
    }

    @Test
    public void removeAll() {
        final List<Object> toBeRemoved = Arrays.asList(
                null,
                generateValue(listType, 2),
                generateValue(listType, 4));
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (!isTypeNullable && listType == BINARY_LIST) {
                    //noinspection unchecked
                    assertFalse(list.removeAll(toBeRemoved)); // since 'equals()' never return true against binary array.
                } else {
                    //noinspection unchecked
                    assertTrue(list.removeAll(toBeRemoved));
                }
            }
        });

        switch (listType) {
            case BINARY_LIST:
                assertEquals(NON_NULL_TEST_SIZE, list.size());
                break;
            case BOOLEAN_LIST:
                assertEquals(NON_NULL_TEST_SIZE / 2, list.size());
                break;
            default:
                assertEquals(NON_NULL_TEST_SIZE - 2, list.size());
                break;
        }
    }

    @Test
    public void removeAll_unsupportedTypeIgnored() {
        final int initialSize = list.size();

        final List<Object> unsupportedValues = Arrays.<Object>asList(
                new int[] {0},
                new StringBuilder("0")
        );
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //noinspection unchecked
                assertFalse(list.removeAll(unsupportedValues));
            }
        });

        assertEquals(initialSize, list.size());
    }

    @Test
    public void removeAll_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        //noinspection unchecked
        list.removeAll(Collections.emptyList());
    }

    @Test
    public void get_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.get(0);
    }

    @Test
    public void first_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.first();
    }

    @Test
    public void last_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.last();
    }

    @Test
    public void size_afterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });

        thrown.expect(IllegalStateException.class);
        list.size();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void where() {
        list.where();
    }

    @Test
    public void toString_() {
        final StringBuilder sb = new StringBuilder("RealmList<").append(listType.getValueTypeName()).append(">@[");
        final String separator = ",";
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            final Object value = generateValue(listType, i);

            if (value instanceof byte[]) {
                sb.append("byte[").append(((byte[]) value).length).append("]");
            } else {
                sb.append(value);
            }
            sb.append(separator);
            if (isTypeNullable) {
                sb.append("null").append(separator);
            }
        }
        sb.setLength(sb.length() - separator.length());
        sb.append("]");

        assertEquals(sb.toString(), list.toString());
    }

    @Test
    public void toString_AfterContainerObjectRemoved() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });
        assertEquals("RealmList<" + listType.getValueTypeName() + ">@[invalid]", list.toString());
    }

    @Test
    public void deleteAllFromRealm() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                list.deleteAllFromRealm();
            }
        });

        assertEquals(0, list.size());
    }

    @Test
    public void deleteAllFromRealm_outsideTransaction() {
        try {
            list.deleteAllFromRealm();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot modify managed objects outside of a write transaction"));
        }
    }

    @Test
    public void deleteAllFromRealm_emptyList() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                list.deleteAllFromRealm();
            }
        });
        assertEquals(0, list.size());

        // The dogs is empty now.
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                list.deleteAllFromRealm();
            }
        });
        assertEquals(0, list.size());
    }

    @Test
    public void deleteAllFromRealm_invalidListShouldThrow() {
        realm.close();
        realm = null;

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(is("This Realm instance has already been closed, making it unusable."));
        list.deleteAllFromRealm();
    }

    @Test
    public void add_null_nonNullableListThrows() {
        if (isTypeNullable) {
            return;
        }

        realm.beginTransaction();
        final int initialSize = list.size();
        try {
            thrown.expect(IllegalArgumentException.class);
            //noinspection unchecked
            list.add(null);
        } finally {
            assertEquals(initialSize, list.size());
        }
    }

    @Test
    public void addAt_null_nonNullableListThrows() {
        if (isTypeNullable) {
            return;
        }

        realm.beginTransaction();
        final int initialSize = list.size();
        try {
            thrown.expect(IllegalArgumentException.class);
            //noinspection unchecked
            list.add(1, null);
        } finally {
            assertEquals(initialSize, list.size());
        }
    }

    @Test
    public void set_null_nonNullableListThrows() {
        if (isTypeNullable) {
            return;
        }

        realm.beginTransaction();
        final int initialSize = list.size();
        try {
            thrown.expect(IllegalArgumentException.class);
            //noinspection unchecked
            list.set(0, null);
        } finally {
            assertEquals(initialSize, list.size());
        }
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forAddObject() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(1, changes.getInsertions().length);
                assertEquals(0, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(3, changes.getInsertions()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        list.add(generateValue(listType, 100));
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forAddAt() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(1, changes.getInsertions().length);
                assertEquals(0, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getInsertions()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        list.add(1, generateValue(listType, 500));
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forSet() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(0, changes.getDeletions().length);
                assertEquals(1, changes.getChanges().length);
                assertEquals(1, changes.getChanges()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        list.set(1, generateValue(listType, 500));
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forRemoveAt() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(1, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getDeletions()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        if (listType == BINARY_LIST) {
            assertArrayEquals((byte[]) generateValue(listType, 100), (byte[]) list.remove(1));
        } else {
            assertEquals(generateValue(listType, 100), list.remove(1));
        }
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forRemoveObject() {
        if (listType == BINARY_LIST) {
            // 'removeAll()' never remove byte array element since 'equals()' never return true against byte array.
            looperThread.testComplete();
            return;
        }

        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 101));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(1, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getDeletions()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        assertTrue(list.remove(generateValue(listType, 101)));
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forRemoveAll() {
        if (listType == BINARY_LIST) {
            // 'removeAll()' never remove byte array element since 'equals()' never return true against byte array.
            looperThread.testComplete();
            return;
        }

        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(listType == BOOLEAN_LIST ? 3 : 2, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getDeletionRanges().length);
                assertEquals(listType == BOOLEAN_LIST ? 0 : 1, changes.getDeletionRanges()[0].startIndex);
                assertEquals(listType == BOOLEAN_LIST ? 3 : 2, changes.getDeletionRanges()[0].length);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked

        final boolean removed = list.removeAll(Arrays.asList(generateValue(listType, 100), generateValue(listType, 200), generateValue(listType, 300)));
        assertTrue(removed);
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forDeleteAt() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(1, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getDeletions()[0]);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        list.deleteFromRealm(1);
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_forDeleteAll() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);

                // add 3 elements as an initial data
                //noinspection unchecked
                list.add(generateValue(listType, 0));
                //noinspection unchecked
                list.add(generateValue(listType, 100));
                //noinspection unchecked
                list.add(generateValue(listType, 200));
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                assertEquals(0, listenerCalledCount.getAndIncrement());
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                assertNotNull(changes);
                assertEquals(0, changes.getInsertions().length);
                assertEquals(3, changes.getDeletions().length);
                assertEquals(0, changes.getChanges().length);
                assertEquals(1, changes.getDeletionRanges().length);
                assertEquals(0, changes.getDeletionRanges()[0].startIndex);
                assertEquals(3, changes.getDeletionRanges()[0].length);
                assertEquals(1, listenerCalledCount.getAndIncrement());
            }
        });

        realm.beginTransaction();
        //noinspection unchecked
        list.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(2, listenerCalledCount.get());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);
            }
        });

        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                fail();
            }
        });
        //noinspection unchecked
        list.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                fail();
            }
        });

        list.removeAllChangeListeners();

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        // This one is added after removal, so it should be triggered.
        //noinspection unchecked
        list.addChangeListener(new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                listenerCalledCount.incrementAndGet();
                looperThread.testComplete();
            }
        });

        // This should trigger the listener if there is any.
        realm.beginTransaction();
        //noinspection unchecked
        list.add(generateValue(listType, 500));
        realm.commitTransaction();

        assertEquals(1, listenerCalledCount.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    @RunTestInLooperThread
    public void removeChangeListener() {
        Realm realm = looperThread.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object = realm.createObject(NullTypes.class, 1000);
                list = getListFor(object, listType, isTypeNullable);
            }
        });

        final AtomicInteger listenerCalledCount = new AtomicInteger(0);
        RealmChangeListener<RealmList<Object>> listener1 = new RealmChangeListener<RealmList<Object>>() {
            @Override
            public void onChange(RealmList<Object> element) {
                fail();
            }
        };
        OrderedRealmCollectionChangeListener<RealmList<Object>> listener2 =
                new OrderedRealmCollectionChangeListener<RealmList<Object>>() {
                    @Override
                    public void onChange(RealmList<Object> collection, @Nullable OrderedCollectionChangeSet changes) {
                        assertEquals(0, listenerCalledCount.getAndIncrement());
                        looperThread.testComplete();
                    }
                };

        list.addChangeListener(listener1);
        list.addChangeListener(listener2);

        list.removeChangeListener(listener1);

        // This should trigger the listener if there is any.
        realm.beginTransaction();
        list.add(generateValue(listType, 500));
        realm.commitTransaction();
        assertEquals(1, listenerCalledCount.get());
    }

    @Test
    public void createSnapshot() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage(is(RealmList.ALLOWED_ONLY_FOR_REALM_MODEL_ELEMENT_MESSAGE));
        list.createSnapshot();
    }
}
