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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.ManagedRealmListForValueTests.ListType;
import io.realm.entities.NullTypes;
import io.realm.rule.RunInLooperThread;
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
import static io.realm.ManagedRealmListForValueTests.NON_NULL_TEST_SIZE;
import static io.realm.ManagedRealmListForValueTests.NULLABLE_TEST_SIZE;
import static io.realm.ManagedRealmListForValueTests.generateValue;
import static io.realm.ManagedRealmListForValueTests.getListFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests specific for RealmList with value elements.
 */
@RunWith(Parameterized.class)
public class ManagedRealmListForValue_toArrayTests extends CollectionTests {

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
    public Boolean typeIsNullable;

    private Realm realm;
    private RealmList list;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        final RealmConfiguration.Builder configurationBuilder = configFactory.createConfigurationBuilder();
        configurationBuilder.schema(NullTypes.class);
        RealmConfiguration realmConfig = configurationBuilder.build();

        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        final NullTypes object = realm.createObject(NullTypes.class, 0);
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

        list = getListFor(object, listType, typeIsNullable);
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    private static final Set<ListType> NON_NUMBER_LIST_TYPES;

    static {
        final HashSet<ListType> set = new HashSet<>();
        set.add(STRING_LIST);
        set.add(BOOLEAN_LIST);
        set.add(BINARY_LIST);
        set.add(DATE_LIST);

        NON_NUMBER_LIST_TYPES = Collections.unmodifiableSet(set);
    }

    @Test
    public void toLongArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toLongArray();
            // should not reach here
            return;
        }

        final long[] expected = new long[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (long) generateValue(LONG_LIST, i);
                expected[i * 2 + 1] = 0L;
            } else {
                expected[i] = (long) generateValue(LONG_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toLongArray()));
    }

    @Test
    public void toLongArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final long valueForNull = Long.MIN_VALUE;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toLongArray(valueForNull);
            // should not reach here
            return;
        }

        final long[] expected = new long[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (long) generateValue(LONG_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (long) generateValue(LONG_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toLongArray(valueForNull)));
    }

    @Test
    public void toIntArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toIntArray();
            // should not reach here
            return;
        }

        final int[] expected = new int[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (int) generateValue(INTEGER_LIST, i);
                expected[i * 2 + 1] = 0;
            } else {
                expected[i] = (int) generateValue(INTEGER_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toIntArray()));
    }

    @Test
    public void toIntArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final int valueForNull = Integer.MIN_VALUE;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toIntArray(valueForNull);
            // should not reach here
            return;
        }

        final int[] expected = new int[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (int) generateValue(INTEGER_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (int) generateValue(INTEGER_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toIntArray(valueForNull)));
    }

    @Test
    public void toShortArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toShortArray();
            // should not reach here
            return;
        }

        final short[] expected = new short[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (short) generateValue(SHORT_LIST, i);
                expected[i * 2 + 1] = 0;
            } else {
                expected[i] = (short) generateValue(SHORT_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toShortArray()));
    }

    @Test
    public void toShortArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final short valueForNull = Short.MIN_VALUE;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toShortArray(valueForNull);
            // should not reach here
            return;
        }

        final short[] expected = new short[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (short) generateValue(SHORT_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (short) generateValue(SHORT_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toShortArray(valueForNull)));
    }

    @Test
    public void toByteArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toByteArray();
            // should not reach here
            return;
        }

        final byte[] expected = new byte[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (byte) generateValue(BYTE_LIST, i);
                expected[i * 2 + 1] = 0;
            } else {
                expected[i] = (byte) generateValue(BYTE_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toByteArray()));
    }

    @Test
    public void toByteArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final byte valueForNull = Byte.MIN_VALUE;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toByteArray(valueForNull);
            // should not reach here
            return;
        }

        final byte[] expected = new byte[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (byte) generateValue(BYTE_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (byte) generateValue(BYTE_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toByteArray(valueForNull)));
    }

    @Test
    public void toDoubleArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toDoubleArray();
            // should not reach here
            return;
        }

        final double[] expected = new double[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (double) generateValue(DOUBLE_LIST, i);
                expected[i * 2 + 1] = 0D;
            } else {
                expected[i] = (double) generateValue(DOUBLE_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toDoubleArray()));
    }

    @Test
    public void toDoubleArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final double valueForNull = Double.NEGATIVE_INFINITY;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toDoubleArray(valueForNull);
            // should not reach here
            return;
        }

        final double[] expected = new double[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (double) generateValue(DOUBLE_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (double) generateValue(DOUBLE_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toDoubleArray(valueForNull)));
    }

    @Test
    public void toFloatArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toFloatArray();
            // should not reach here
            return;
        }

        final float[] expected = new float[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (float) generateValue(FLOAT_LIST, i);
                expected[i * 2 + 1] = 0L;
            } else {
                expected[i] = (float) generateValue(FLOAT_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toFloatArray()));
    }

    @Test
    public void toFloatArrayWithNullValue() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        final float valueForNull = Float.NEGATIVE_INFINITY;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toFloatArray(valueForNull);
            // should not reach here
            return;
        }

        final float[] expected = new float[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (float) generateValue(FLOAT_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (float) generateValue(FLOAT_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toFloatArray(valueForNull)));
    }

    @Test
    public void toBooleanArray() {
        final boolean exceptionExpected = (listType != BOOLEAN_LIST);

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toBooleanArray();
            // should not reach here
            return;
        }

        final boolean[] expected = new boolean[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (boolean) generateValue(BOOLEAN_LIST, i);
                expected[i * 2 + 1] = false;
            } else {
                expected[i] = (boolean) generateValue(BOOLEAN_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toBooleanArray()));
    }

    @Test
    public void toBooleanArrayWithNullValue() {
        final boolean exceptionExpected = (listType != BOOLEAN_LIST);

        final boolean valueForNull = true;

        if (exceptionExpected) {
            thrown.expect(IllegalStateException.class);
            list.toBooleanArray(valueForNull);
            // should not reach here
            return;
        }

        final boolean[] expected = new boolean[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (boolean) generateValue(BOOLEAN_LIST, i);
                expected[i * 2 + 1] = valueForNull;
            } else {
                expected[i] = (boolean) generateValue(BOOLEAN_LIST, i);
            }
        }
        assertTrue(Arrays.equals(expected, list.toBooleanArray(valueForNull)));
    }

    @Test
    public void toArray() {
        final Object[] expected = new Object[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = generateValue(listType, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = generateValue(listType, i);
            }
        }

        if (listType != BINARY_LIST) {
            assertTrue(Arrays.equals(expected, list.toArray()));
        } else {
            final Object[] array = list.toArray();
            assertEquals(expected.length, array.length);
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] == null) {
                    assertNull(array[i]);
                } else {
                    assertTrue(array[i] instanceof byte[]);
                    assertTrue(Arrays.equals((byte[]) expected[i], (byte[]) array[i]));
                }
            }
        }
    }

    @Test
    public void toArray_withStringArray() {
        final boolean exceptionExpected = (listType != STRING_LIST);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new String[0]);
            // should not reach here
            return;
        }

        final String[] expected = new String[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (String) generateValue(STRING_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (String) generateValue(STRING_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new String[0]);
        assertEquals(String.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withBooleanArray() {
        final boolean exceptionExpected = (listType != BOOLEAN_LIST);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Boolean[0]);
            // should not reach here
            return;
        }

        final Boolean[] expected = new Boolean[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Boolean) generateValue(BOOLEAN_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Boolean) generateValue(BOOLEAN_LIST, i);
            }
        }

        final Object[] returnedArray = list.toArray(new Boolean[0]);
        assertEquals(Boolean.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withBinaryArray() {
        final boolean exceptionExpected = (listType != BINARY_LIST);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new byte[0][]);
            // should not reach here
            return;
        }

        final byte[][] expected = new byte[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE][];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (byte[]) generateValue(BINARY_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (byte[]) generateValue(BINARY_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new byte[0][]);
        assertEquals(byte[].class, returnedArray.getClass().getComponentType());

        assertEquals(expected.length, returnedArray.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] == null) {
                assertNull(returnedArray[i]);
            } else {
                assertTrue(returnedArray[i] instanceof byte[]);
                assertTrue(Arrays.equals(expected[i], (byte[]) returnedArray[i]));
            }
        }
    }

    @Test
    public void toArray_withLongArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Long[0]);
            // should not reach here
            return;
        }

        final Long[] expected = new Long[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Long) generateValue(LONG_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Long) generateValue(LONG_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Long[0]);
        assertEquals(Long.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withIntegerArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Integer[0]);
            // should not reach here
            return;
        }

        final Integer[] expected = new Integer[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Integer) generateValue(INTEGER_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Integer) generateValue(INTEGER_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Integer[0]);
        assertEquals(Integer.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withShortArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Short[0]);
            // should not reach here
            return;
        }

        final Short[] expected = new Short[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Short) generateValue(SHORT_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Short) generateValue(SHORT_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Short[0]);
        assertEquals(Short.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withByteArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Byte[0]);
            // should not reach here
            return;
        }

        final Byte[] expected = new Byte[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Byte) generateValue(BYTE_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Byte) generateValue(BYTE_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Byte[0]);
        assertEquals(Byte.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withDoubleArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Double[0]);
            // should not reach here
            return;
        }

        final Double[] expected = new Double[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Double) generateValue(DOUBLE_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Double) generateValue(DOUBLE_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Double[0]);
        assertEquals(Double.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withFloatArray() {
        final boolean exceptionExpected = NON_NUMBER_LIST_TYPES.contains(listType);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Float[0]);
            // should not reach here
            return;
        }

        final Float[] expected = new Float[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Float) generateValue(FLOAT_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Float) generateValue(FLOAT_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Float[0]);
        assertEquals(Float.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }

    @Test
    public void toArray_withDateArray() {
        final boolean exceptionExpected = (listType != DATE_LIST);

        if (exceptionExpected) {
            thrown.expect(ArrayStoreException.class);
            list.toArray(new Date[0]);
            // should not reach here
            return;
        }

        final Date[] expected = new Date[typeIsNullable ? NULLABLE_TEST_SIZE : NON_NULL_TEST_SIZE];
        for (int i = 0; i < NON_NULL_TEST_SIZE; i++) {
            if (typeIsNullable) {
                expected[i * 2] = (Date) generateValue(DATE_LIST, i);
                expected[i * 2 + 1] = null;
            } else {
                expected[i] = (Date) generateValue(DATE_LIST, i);
            }
        }
        final Object[] returnedArray = list.toArray(new Date[0]);
        assertEquals(Date.class, returnedArray.getClass().getComponentType());
        assertTrue(Arrays.equals(expected, returnedArray));
    }
}