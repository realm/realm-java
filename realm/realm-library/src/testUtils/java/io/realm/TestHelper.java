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

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.NullTypes;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.internal.OsResults;
import io.realm.internal.OsObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.Table;
import io.realm.internal.Util;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.LogLevel;
import io.realm.log.RealmLogger;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class TestHelper {
    public static final int VERY_SHORT_WAIT_SECS = 1;
    public static final int SHORT_WAIT_SECS = 10;
    public static final int STANDARD_WAIT_SECS = 100;

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Random RANDOM = new Random();

    public static class ExpectedCountCallback implements RealmCache.Callback {

        private int expectedCount;

        ExpectedCountCallback(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        @Override
        public void onResult(int count) {
            assertEquals(expectedCount, count);
        }
    }

    public static RealmFieldType getColumnType(Object o) {
        if (o instanceof Boolean) {
            return RealmFieldType.BOOLEAN;
        }
        if (o instanceof String) {
            return RealmFieldType.STRING;
        }
        if (o instanceof Long) {
            return RealmFieldType.INTEGER;
        }
        if (o instanceof Float) {
            return RealmFieldType.FLOAT;
        }
        if (o instanceof Double) {
            return RealmFieldType.DOUBLE;
        }
        if (o instanceof Date) {
            return RealmFieldType.DATE;
        }
        if (o instanceof byte[]) {
            return RealmFieldType.BINARY;
        }

        throw new IllegalArgumentException("Unsupported type");
    }

    /**
     * Appends the specified row to the end of the table. For internal testing usage only.
     *
     * @param table the table where the object to be added.
     * @param values values.
     * @return the row index of the appended row.
     * @deprecated Remove this functions since it doesn't seem to be useful. And this function does deal with tables
     * with primary key defined well. Primary key has to be set with `setXxxUnique` as the first thing to do after row
     * added.
     */
    public static long addRowWithValues(Table table, Object... values) {
        long rowIndex = OsObject.createRow(table);

        // Checks values types.
        int columns = (int) table.getColumnCount();
        if (columns != values.length) {
            throw new IllegalArgumentException("The number of value parameters (" +
                    String.valueOf(values.length) +
                    ") does not match the number of columns in the table (" +
                    String.valueOf(columns) + ").");
        }
        RealmFieldType[] colTypes = new RealmFieldType[columns];
        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[columnIndex];
            RealmFieldType colType = table.getColumnType(columnIndex);
            colTypes[columnIndex] = colType;
            if (!colType.isValid(value)) {
                // String representation of the provided value type.
                String providedType;
                if (value == null) {
                    providedType = "null";
                } else {
                    providedType = value.getClass().toString();
                }

                throw new IllegalArgumentException("Invalid argument no " + String.valueOf(1 + columnIndex) +
                        ". Expected a value compatible with column type " + colType + ", but got " + providedType + ".");
            }
        }

        // Inserts values.
        for (long columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[(int) columnIndex];
            switch (colTypes[(int) columnIndex]) {
                case BOOLEAN:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setBoolean(columnIndex, rowIndex, (Boolean) value, false);
                    }
                    break;
                case INTEGER:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        long longValue = ((Number) value).longValue();
                        table.setLong(columnIndex, rowIndex, longValue, false);
                    }
                    break;
                case FLOAT:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setFloat(columnIndex, rowIndex, (Float) value, false);
                    }
                    break;
                case DOUBLE:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setDouble(columnIndex, rowIndex, (Double) value, false);
                    }
                    break;
                case STRING:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setString(columnIndex, rowIndex, (String) value, false);
                    }
                    break;
                case DATE:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setDate(columnIndex, rowIndex, (Date) value, false);
                    }
                    break;
                case BINARY:
                    if (value == null) {
                        table.setNull(columnIndex, rowIndex, false);
                    } else {
                        table.setBinaryByteArray(columnIndex, rowIndex, (byte[]) value, false);
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected columnType: " + String.valueOf(colTypes[(int) columnIndex]));
            }
        }
        return rowIndex;
    }

    /**
     * Creates an empty table whose name is "temp" with 1 column of all our supported column types, currently 7 columns.
     *
     * @param sharedRealm A {@link OsSharedRealm} where the table is created.
     * @return created table.
     */
    public static Table createTableWithAllColumnTypes(OsSharedRealm sharedRealm) {
        return createTableWithAllColumnTypes(sharedRealm, "temp");
    }

    /**
     * Creates an empty table with 1 column of all our supported column types, currently 7 columns.
     *
     * @param sharedRealm A {@link OsSharedRealm} where the table is created.
     * @param name name of the table.
     * @return created table.
     */
    @SuppressWarnings("WeakerAccess")
    public static Table createTableWithAllColumnTypes(OsSharedRealm sharedRealm,
            @SuppressWarnings("SameParameterValue") String name) {
        boolean wasInTransaction = sharedRealm.isInTransaction();
        if (!wasInTransaction) {
            sharedRealm.beginTransaction();
        }
        try {
            Table t = sharedRealm.createTable(name);

            t.addColumn(RealmFieldType.BINARY, "binary");
            t.addColumn(RealmFieldType.BOOLEAN, "boolean");
            t.addColumn(RealmFieldType.DATE, "date");
            t.addColumn(RealmFieldType.DOUBLE, "double");
            t.addColumn(RealmFieldType.FLOAT, "float");
            t.addColumn(RealmFieldType.INTEGER, "long");
            t.addColumn(RealmFieldType.STRING, "string");

            return t;
        } catch (RuntimeException e) {
            if (!wasInTransaction) {
                sharedRealm.cancelTransaction();
            }
            throw e;
        } finally {
            if (!wasInTransaction && sharedRealm.isInTransaction()) {
                sharedRealm.commitTransaction();
            }
        }
    }

    public static Table createTable(OsSharedRealm sharedRealm, String name) {
        return createTable(sharedRealm, name, null);
    }

    public interface AdditionalTableSetup {
        void execute(Table table);
    }

    public static Table createTable(OsSharedRealm sharedRealm, String name, AdditionalTableSetup additionalSetup) {
        boolean wasInTransaction = sharedRealm.isInTransaction();
        if (!wasInTransaction) {
            sharedRealm.beginTransaction();
        }
        try {
            Table table = sharedRealm.createTable(name);
            if (additionalSetup != null) {
                additionalSetup.execute(table);
            }
            return table;
        } catch (RuntimeException e) {
            if (!wasInTransaction) {
                sharedRealm.cancelTransaction();
            }
            throw e;
        } finally {
            if (!wasInTransaction && sharedRealm.isInTransaction()) {
                sharedRealm.commitTransaction();
            }
        }
    }

    public static String streamToString(InputStream in) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(in, UTF_8));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return sb.toString();
    }

    public static InputStream stringToStream(String str) {
        return new ByteArrayInputStream(str.getBytes(UTF_8));
    }

    // Returns a random key used by encrypted Realms.
    public static byte[] getRandomKey() {
        byte[] key = new byte[64];
        RANDOM.nextBytes(key);
        return key;
    }

    public static String getRandomEmail() {
        StringBuilder sb = new StringBuilder(UUID.randomUUID().toString().toLowerCase());
        sb.append('@');
        sb.append("androidtest.realm.io");
        return sb.toString();
    }

    // Returns a random key from the given seed. Used by encrypted Realms.
    public static byte[] getRandomKey(long seed) {
        byte[] key = new byte[64];
        new Random(seed).nextBytes(key);
        return key;
    }

    /**
     * Returns a RealmLogger that will fail if it is asked to log a message above a certain level.
     *
     * @param failureLevel level at which the unit test will fail: {@see Log}.
     * @return RealmLogger implementation
     */
    public static RealmLogger getFailureLogger(final int failureLevel) {
        return new RealmLogger() {
            private void failIfEqualOrAbove(int logLevel) {
                if (logLevel >= failureLevel) {
                    fail("Message logged that was above valid level: " + logLevel + " >= " + failureLevel);
                }
            }

            @Override
            public void log(int level, String tag, Throwable throwable, String message) {
                failIfEqualOrAbove(level);
            }
        };
    }

    // Generate a random string with only capital letters which is always a valid class/field name.
    public static String getRandomString(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (r.nextInt(26) + 'A')); // Restrict to capital letters
        }
        return sb.toString();
    }

    /**
     * Returns a naive logger that can be used to test the values that are sent to the logger.
     */
    public static class TestLogger implements RealmLogger {

        private final int minimumLevel;
        public String message;
        public Throwable throwable;

        public TestLogger() {
            this(LogLevel.DEBUG);
        }

        public TestLogger(int minimumLevel) {
            this.minimumLevel = minimumLevel;
        }

        @Override
        public void log(int level, String tag, Throwable throwable, String message) {
            if (minimumLevel <= level) {
                this.message = message;
                this.throwable = throwable;
            }
        }
    }

    public static class StubInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return 0; // Stub implementation
        }
    }

    // Allocs as much garbage as we can. Pass maxSize = 0 to use it.
    public static byte[] allocGarbage(int garbageSize) {
        if (garbageSize == 0) {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            garbageSize = (int) (maxMemory - totalMemory) / 10 * 9;
        }
        byte garbage[] = new byte[0];
        try {
            if (garbageSize > 0) {
                garbage = new byte[garbageSize];
                garbage[0] = 1;
                garbage[garbage.length - 1] = 1;
            }
        } catch (OutOfMemoryError oom) {
            return allocGarbage(garbageSize / 10 * 9);
        }

        return garbage;
    }

    // Creates SHA512 hash of a String. Can be used as password for encrypted Realms.
    public static byte[] SHA512(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(str.getBytes(UTF_8), 0, str.length());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated Use {@link TestRealmConfigurationFactory#createConfiguration()} instead.
     */
    @Deprecated
    public static RealmConfiguration createConfiguration(Context context) {
        return createConfiguration(context, Realm.DEFAULT_REALM_NAME);
    }

    /**
     * @deprecated Use {@link TestRealmConfigurationFactory#createConfiguration(String)} instead.
     */
    @Deprecated
    public static RealmConfiguration createConfiguration(Context context, String name) {
        return createConfiguration(context.getFilesDir(), name);
    }

    /**
     * @deprecated Use {@link TestRealmConfigurationFactory#createConfiguration(String)} instead.
     */
    @Deprecated
    public static RealmConfiguration createConfiguration(File folder, String name) {
        return createConfiguration(folder, name, null);
    }

    /**
     * @deprecated Use {@link TestRealmConfigurationFactory#createConfiguration(String, byte[])} instead.
     */
    @Deprecated
    public static RealmConfiguration createConfiguration(Context context, String name, byte[] key) {
        return createConfiguration(context.getFilesDir(), name, key);
    }

    /**
     * @deprecated Use {@link TestRealmConfigurationFactory#createConfiguration(String, byte[])} instead.
     */
    @Deprecated
    public static RealmConfiguration createConfiguration(File dir, String name, byte[] key) {
        RealmConfiguration.Builder config = new RealmConfiguration.Builder(InstrumentationRegistry.getTargetContext())
                .directory(dir)
                .name(name);
        if (key != null) {
            config.encryptionKey(key);
        }

        return config.build();
    }

    /**
     * Adds a String type PrimaryKey object to a realm with values for name field (PrimaryKey) and id field
     */
    public static PrimaryKeyAsString addStringPrimaryKeyObjectToTestRealm(Realm testRealm, String primaryFieldValue, long secondaryFieldValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsString obj = new PrimaryKeyAsString();
        obj.setName(primaryFieldValue);
        obj.setId(secondaryFieldValue);
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    /**
     * Populates a realm with String type Primarykey objects for a number of numberOfPopulation - 1,
     * starting with iteratorBeginValue. One object is setup to have given values from parameters.
     */
    public static void populateTestRealmWithStringPrimaryKey(Realm testRealm, String primaryFieldValue, long secondaryFieldValue, int numberOfPopulation, int iteratorBeginValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsString userObj = new PrimaryKeyAsString();
        userObj.setName(primaryFieldValue);
        userObj.setId(secondaryFieldValue);
        testRealm.copyToRealm(userObj);
        int idValue = iteratorBeginValue;
        for (int i = 0; i < numberOfPopulation - 1; ++i, ++idValue) {
            PrimaryKeyAsString obj = new PrimaryKeyAsString();
            obj.setName(String.valueOf(idValue));
            obj.setId(idValue);
            testRealm.copyToRealm(obj);
        }
        testRealm.commitTransaction();
    }

    /**
     * Adds a Byte type PrimaryKey object to a realm with values for id field (PrimaryKey) and name field
     */
    public static PrimaryKeyAsBoxedByte addBytePrimaryKeyObjectToTestRealm(Realm testRealm, Byte primaryFieldValue, String secondaryFieldValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedByte obj = new PrimaryKeyAsBoxedByte();
        obj.setId(primaryFieldValue);
        obj.setName(secondaryFieldValue);
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    /**
     * Populates a realm with Byte type Primarykey objects for a number of numberOfPopulation - 1,
     * starting with iteratorBeginValue. One object is setup to have given values from parameters.
     */
    public static void populateTestRealmWithBytePrimaryKey(Realm testRealm, Byte primaryFieldValue, String secondaryFieldValue, int numberOfPopulation, int iteratorBeginValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedByte userObj = new PrimaryKeyAsBoxedByte();
        userObj.setId(primaryFieldValue);
        userObj.setName(secondaryFieldValue);
        testRealm.copyToRealm(userObj);
        byte idValue = (byte) iteratorBeginValue;
        for (int i = 0; i < numberOfPopulation - 1; ++i, ++idValue) {
            PrimaryKeyAsBoxedByte obj = new PrimaryKeyAsBoxedByte();
            obj.setId(idValue);
            obj.setName(String.valueOf(idValue));
            testRealm.copyToRealm(obj);
        }
        testRealm.commitTransaction();
    }

    /**
     * Adds a Short type PrimaryKey object to a realm with values for id field (PrimaryKey) and name field
     */
    public static PrimaryKeyAsBoxedShort addShortPrimaryKeyObjectToTestRealm(Realm testRealm, Short primaryFieldValue, String secondaryFieldValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedShort obj = new PrimaryKeyAsBoxedShort();
        obj.setId(primaryFieldValue);
        obj.setName(secondaryFieldValue);
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    /**
     * Populates a realm with Short type Primarykey objects for a number of numberOfPopulation - 1,
     * starting with iteratorBeginValue. One object is setup to have given values from parameters.
     */
    public static void populateTestRealmWithShortPrimaryKey(Realm testRealm, Short primaryFieldValue, String secondaryFieldValue, int numberOfPopulation, int iteratorBeginValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedShort userObj = new PrimaryKeyAsBoxedShort();
        userObj.setId(primaryFieldValue);
        userObj.setName(secondaryFieldValue);
        testRealm.copyToRealm(userObj);
        short idValue = (short)iteratorBeginValue;
        for (int i = 0; i < numberOfPopulation - 1; ++i, ++idValue) {
            PrimaryKeyAsBoxedShort obj = new PrimaryKeyAsBoxedShort();
            obj.setId(idValue);
            obj.setName(String.valueOf(idValue));
            testRealm.copyToRealm(obj);
        }
        testRealm.commitTransaction();
    }

    /**
     * Adds a Integer type PrimaryKey object to a realm with values for id field (PrimaryKey) and name field
     */
    public static PrimaryKeyAsBoxedInteger addIntegerPrimaryKeyObjectToTestRealm(Realm testRealm, Integer primaryFieldValue, String secondaryFieldValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedInteger obj = new PrimaryKeyAsBoxedInteger();
        obj.setId(primaryFieldValue);
        obj.setName(secondaryFieldValue);
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    /**
     * Populates a realm with Integer type Primarykey objects for a number of numberOfPopulation - 1,
     * starting with iteratorBeginValue. One object is setup to have given values from parameters.
     */
    public static void populateTestRealmWithIntegerPrimaryKey(Realm testRealm, Integer primaryFieldValue, String secondaryFieldValue, int numberOfPopulation, int iteratorBeginValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedInteger userObj = new PrimaryKeyAsBoxedInteger();
        userObj.setId(primaryFieldValue);
        userObj.setName(secondaryFieldValue);
        testRealm.copyToRealm(userObj);
        int idValue = iteratorBeginValue;
        for (int i = 0; i < numberOfPopulation - 1; ++i, ++idValue) {
            PrimaryKeyAsBoxedInteger obj = new PrimaryKeyAsBoxedInteger();
            obj.setId(idValue);
            obj.setName(String.valueOf(idValue));
            testRealm.copyToRealm(obj);
        }
        testRealm.commitTransaction();
    }

    /**
     * Adds a Long type PrimaryKey object to a realm with values for id field (PrimaryKey) and name field
     */
    public static PrimaryKeyAsBoxedLong addLongPrimaryKeyObjectToTestRealm(Realm testRealm, Long primaryFieldValue, String secondaryFieldValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedLong obj = new PrimaryKeyAsBoxedLong();
        obj.setId(primaryFieldValue);
        obj.setName(secondaryFieldValue);
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    /**
     * Populates a realm with Long type Primarykey objects for a number of numberOfPopulation - 1,
     * starting with iteratorBeginValue. One object is setup to have given values from parameters.
     */
    public static void populateTestRealmWithLongPrimaryKey(Realm testRealm, Long primaryFieldValue, String secondaryFieldValue, long numberOfPopulation, long iteratorBeginValue) {
        testRealm.beginTransaction();
        PrimaryKeyAsBoxedLong userObj = new PrimaryKeyAsBoxedLong();
        userObj.setId(primaryFieldValue);
        userObj.setName(secondaryFieldValue);
        testRealm.copyToRealm(userObj);
        long idValue = iteratorBeginValue;
        for (long i = 0; i < numberOfPopulation - 1; ++i, ++idValue) {
            PrimaryKeyAsBoxedLong obj = new PrimaryKeyAsBoxedLong();
            obj.setId(idValue);
            obj.setName(String.valueOf(idValue));
            testRealm.copyToRealm(obj);
        }
        testRealm.commitTransaction();
    }

    public static void populateTestRealmForNullTests(Realm testRealm) {

        // Creates 3 NullTypes objects. The objects are self-referenced (link) in
        // order to test link queries.
        //
        // +-+--------+------+---------+--------+--------------------+
        // | | string | link | numeric | binary | numeric (not null) |
        // +-+--------+------+---------+--------+--------------------+
        // |0| Fish   |    0 |       1 |    {0} |                  1 |
        // |1| null   | null |    null |   null |                  0 |
        // |2| Horse  |    1 |       3 |  {1,2} |                  3 |
        // +-+--------+------+---------+--------+--------------------+

        // 1 String
        String[] words = {"Fish", null, "Horse"};
        // 2 Bytes
        byte[][] binaries = {new byte[]{0}, null, new byte[]{1, 2}};
        // 3 Boolean
        Boolean[] booleans = {false, null, true};
        // Numeric fields will be 1, 0/null, 3
        // 10 Date
        Date[] dates = {new Date(0), null, new Date(10000)};
        NullTypes[] nullTypesArray = new NullTypes[3];

        testRealm.beginTransaction();
        for (int i = 0; i < 3; i++) {
            NullTypes nullTypes = new NullTypes();
            nullTypes.setId(i + 1);
            // 1 String
            nullTypes.setFieldStringNull(words[i]);
            if (words[i] != null) {
                nullTypes.setFieldStringNotNull(words[i]);
            }
            // 2 Bytes
            nullTypes.setFieldBytesNull(binaries[i]);
            if (binaries[i] != null) {
                nullTypes.setFieldBytesNotNull(binaries[i]);
            }
            // 3 Boolean
            nullTypes.setFieldBooleanNull(booleans[i]);
            if (booleans[i] != null) {
                nullTypes.setFieldBooleanNotNull(booleans[i]);
            }
            if (i != 1) {
                int n = i + 1;
                // 4 Byte
                nullTypes.setFieldByteNull((byte) n);
                nullTypes.setFieldByteNotNull((byte) n);
                // 5 Short
                nullTypes.setFieldShortNull((short) n);
                nullTypes.setFieldShortNotNull((short) n);
                // 6 Integer
                nullTypes.setFieldIntegerNull(n);
                nullTypes.setFieldIntegerNotNull(n);
                // 7 Long
                nullTypes.setFieldLongNull((long) n);
                nullTypes.setFieldLongNotNull((long) n);
                // 8 Float
                nullTypes.setFieldFloatNull((float) n);
                nullTypes.setFieldFloatNotNull((float) n);
                // 9 Double
                nullTypes.setFieldDoubleNull((double) n);
                nullTypes.setFieldDoubleNotNull((double) n);
            }
            // 10 Date
            nullTypes.setFieldDateNull(dates[i]);
            if (dates[i] != null) {
                nullTypes.setFieldDateNotNull(dates[i]);
            }

            nullTypesArray[i] = testRealm.copyToRealm(nullTypes);
        }
        nullTypesArray[0].setFieldObjectNull(nullTypesArray[0]);
        nullTypesArray[1].setFieldObjectNull(null);
        nullTypesArray[2].setFieldObjectNull(nullTypesArray[1]);
        testRealm.commitTransaction();
    }

    public static void populateAllNonNullRowsForNumericTesting(Realm realm) {
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(3);
        nullTypes1.setFieldFloatNull(4F);
        nullTypes1.setFieldDoubleNull(5D);
        nullTypes1.setFieldBooleanNull(true);
        nullTypes1.setFieldStringNull("4");
        nullTypes1.setFieldDateNull(new Date(12345));

        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);
        nullTypes2.setFieldIntegerNull(-1);
        nullTypes2.setFieldFloatNull(-2F);
        nullTypes2.setFieldDoubleNull(-3D);
        nullTypes2.setFieldBooleanNull(false);
        nullTypes2.setFieldStringNull("str");
        nullTypes2.setFieldDateNull(new Date(-2000));

        NullTypes nullTypes3 = new NullTypes();
        nullTypes3.setId(3);
        nullTypes3.setFieldIntegerNull(4);
        nullTypes3.setFieldFloatNull(5F);
        nullTypes3.setFieldDoubleNull(6D);
        nullTypes3.setFieldBooleanNull(false);
        nullTypes3.setFieldStringNull("0");
        nullTypes3.setFieldDateNull(new Date(0));

        realm.beginTransaction();
        realm.copyToRealm(nullTypes1);
        realm.copyToRealm(nullTypes2);
        realm.copyToRealm(nullTypes3);
        realm.commitTransaction();
    }

    public static void populatePartialNullRowsForNumericTesting(Realm realm) {
        // Id values are [1, 2, 3]
        // IntegerNull values are [3, null, 4]
        // FloatNull values are [4F, null, 5F]
        // DoubleNull values are [5D, null, 6F]
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(3);
        nullTypes1.setFieldFloatNull(4F);
        nullTypes1.setFieldDoubleNull(5D);
        nullTypes1.setFieldBooleanNull(true);
        nullTypes1.setFieldStringNull("4");
        nullTypes1.setFieldDateNull(new Date(12345));

        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);

        NullTypes nullTypes3 = new NullTypes();
        nullTypes3.setId(3);
        nullTypes3.setFieldIntegerNull(4);
        nullTypes3.setFieldFloatNull(5F);
        nullTypes3.setFieldDoubleNull(6D);
        nullTypes3.setFieldBooleanNull(false);
        nullTypes3.setFieldStringNull("0");
        nullTypes3.setFieldDateNull(new Date(0));

        realm.beginTransaction();
        realm.copyToRealm(nullTypes1);
        realm.copyToRealm(nullTypes2);
        realm.copyToRealm(nullTypes3);
        realm.commitTransaction();
    }

    public static void populateAllNullRowsForNumericTesting(Realm realm) {
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);

        realm.beginTransaction();
        realm.copyToRealm(nullTypes1);
        realm.copyToRealm(nullTypes2);
        realm.commitTransaction();
    }

    // Helper function to create all columns except the given excluding field for NullTypes.
    // The schema version will be set to 0.
    public static void initNullTypesTableExcludes(DynamicRealm realm, String excludingField) {
        realm.beginTransaction();

        RealmObjectSchema nullTypesSchema = realm.getSchema().create(NullTypes.CLASS_NAME);
        if (!excludingField.equals(NullTypes.FIELD_ID)) {
            nullTypesSchema.addField(NullTypes.FIELD_ID, int.class, FieldAttribute.PRIMARY_KEY);
        }
        if (!excludingField.equals(NullTypes.FIELD_STRING_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_STRING_NOT_NULL, String.class, FieldAttribute.REQUIRED);
        }
        if (!excludingField.equals(NullTypes.FIELD_STRING_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_STRING_NULL, String.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_BYTES_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BYTES_NOT_NULL, byte[].class, FieldAttribute.REQUIRED);
        }
        if (!excludingField.equals(NullTypes.FIELD_BYTES_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BYTES_NULL, byte[].class);
        }
        if (!excludingField.equals(NullTypes.FIELD_BOOLEAN_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BOOLEAN_NOT_NULL, boolean.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_BOOLEAN_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BOOLEAN_NULL, Boolean.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_BYTE_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BYTE_NOT_NULL, byte.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_BYTE_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_BYTE_NULL, Byte.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_SHORT_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_SHORT_NOT_NULL, short.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_SHORT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_SHORT_NULL, Short.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_INTEGER_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_INTEGER_NOT_NULL, int.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_INTEGER_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_INTEGER_NULL, Integer.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_LONG_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_LONG_NOT_NULL, long.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_LONG_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_LONG_NULL, Long.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_FLOAT_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_FLOAT_NOT_NULL, float.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_FLOAT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_FLOAT_NULL, Float.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_DOUBLE_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_DOUBLE_NOT_NULL, double.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_DOUBLE_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_DOUBLE_NULL, Double.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_DATE_NOT_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_DATE_NOT_NULL, Date.class, FieldAttribute.REQUIRED);
        }
        if (!excludingField.equals(NullTypes.FIELD_DATE_NULL)) {
            nullTypesSchema.addField(NullTypes.FIELD_DATE_NULL, Date.class);
        }
        if (!excludingField.equals(NullTypes.FIELD_OBJECT_NULL)) {
            nullTypesSchema.addRealmObjectField(NullTypes.FIELD_OBJECT_NULL, nullTypesSchema);
        }

        nullTypesSchema.addRealmListField(NullTypes.FIELD_LIST_NULL, nullTypesSchema);

        realm.setVersion(0);
        realm.commitTransaction();
    }

    public static void populateSimpleAllTypesPrimaryKey(Realm realm) {
        realm.beginTransaction();
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        realm.copyToRealm(obj);
        realm.commitTransaction();
    }


    /*
     * Fields order test for Chained or Multi-Arguments Distinct()
     *
     * The idea is to interweave different values in 2's multiplier and 3's multiplier in a way that
     * the outcome is different if the order of distinct* operations alternates. More numbers of
     * fields can be constructed with the combination of multipliers in prime numbers such as 2, 3,
     * and 5.
     *
     * An example is illustrated below.
     *
     * Object      : O1| O2| O3| O4| O5| O6
     * indexString : A | A | B | B | A | A
     * indexLong   : 1 | 1 | 1 | 2 | 2 | 2
     *
     * @param realm a {@link Realm} instance.
     * @param numberOfBlocks number of times set of unique objects should be created.
     */
    public static void populateForDistinctFieldsOrder(Realm realm, long numberOfBlocks) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfBlocks; i++) {
            for (int j = 0; j < 6; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexString((((j / 2) % 2) == 0) ? "A" : "B");
                obj.setIndexLong((j < 3) ? 1 : 2);
            }
        }
        realm.commitTransaction();
    }

    public static void awaitOrFail(CountDownLatch latch) {
        awaitOrFail(latch, STANDARD_WAIT_SECS);
    }

    public static void awaitOrFail(CountDownLatch latch, int numberOfSeconds) {
        try {
            if (android.os.Debug.isDebuggerConnected()) {
                // If we are debugging the tests, just waits without a timeout.
                // Don't want a timeout while we are stopped at a break point.
                latch.await();
            } else if (!latch.await(numberOfSeconds, TimeUnit.SECONDS)) {
                fail("Test took longer than " + numberOfSeconds + " seconds");
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public interface LooperTest {
        CountDownLatch getRealmClosedSignal();
        Looper getLooper();
        Throwable getAssertionError();
    }

    // Cleans resource, shutdowns the executor service and throws any background exception.
    @SuppressWarnings("Finally")
    public static void exitOrThrow(ExecutorService executorService, CountDownLatch testFinishedSignal, LooperTest test) throws Throwable {

        // Waits for the signal indicating the test's use case is done.
        try {
            // Even if this fails we want to try as hard as possible to cleanup. If we fail to close all resources
            // properly, the `after()` method will most likely throw as well because it tries do delete any Realms
            // used. Any exception in the `after()` code will mask the original error.
            TestHelper.awaitOrFail(testFinishedSignal);
        } finally {
            Looper looper = test.getLooper();
            if (looper != null) {
                // Failing to quit the looper will not execute the finally block responsible
                // of closing the Realm.
                looper.quit();
            }

            // Waits for the finally block to execute and closes the Realm.
            TestHelper.awaitOrFail(test.getRealmClosedSignal());
            // Closes the executor.
            // This needs to be called after waiting since it might interrupt waitRealmThreadExecutorFinish().
            executorService.shutdownNow();

            Throwable fault = test.getAssertionError();
            if (fault != null) {
                // Throws any assertion errors happened in the background thread.
                throw fault;
            }
        }
    }

    public static InputStream loadJsonFromAssets(Context context, String file) throws IOException {
        AssetManager assetManager = context.getAssets();
        return assetManager.open(file);
    }

    public static void quitLooperOrFail() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quit();
        } else {
            Assert.fail();
        }
    }

    /**
     * Creates a {@link RealmResults} instance.
     * This helper method is useful to create a mocked {@link RealmResults}.
     *
     * @param realm a {@link Realm} or a {@link DynamicRealm} instance.
     * @param osResults a {@link OsResults} instance.
     * @param tableClass a Class of Table.
     * @return a created {@link RealmResults} instance.
     */
    public static <T extends RealmObject> RealmResults<T> newRealmResults(
            BaseRealm realm, OsResults osResults, Class<T> tableClass) {
        //noinspection TryWithIdenticalCatches
        try {
            final Constructor<RealmResults> c = RealmResults.class.getDeclaredConstructor(
                    BaseRealm.class, OsResults.class, Class.class);
            c.setAccessible(true);
            //noinspection unchecked
            return c.newInstance(realm, osResults, tableClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void testNoObjectFound(
            Realm realm,
            Class<? extends RealmModel> clazz,
            String fieldName, Object value) {
        testObjectCount(realm, 0L, clazz, fieldName, value);
    }

    public static void testOneObjectFound(
            Realm realm,
            Class<? extends RealmModel> clazz,
            String fieldName, Object value) {
        testObjectCount(realm, 1L, clazz, fieldName, value);
    }

    public static void testObjectCount(
            Realm realm,
            long expectedCount,
            Class<? extends RealmModel> clazz,
            String fieldName, Object value) {
        final RealmQuery<? extends RealmModel> query;
        switch (value.getClass().getSimpleName()) {
            case "String":
                query = realm.where(clazz).equalTo(fieldName, (String) value);
                break;
            case "Byte":
                query = realm.where(clazz).equalTo(fieldName, (Byte) value);
                break;
            case "Short":
                query = realm.where(clazz).equalTo(fieldName, (Short) value);
                break;
            case "Integer":
                query = realm.where(clazz).equalTo(fieldName, (Integer) value);
                break;
            case "Long":
                query = realm.where(clazz).equalTo(fieldName, (Long) value);
                break;
            case "Float":
                query = realm.where(clazz).equalTo(fieldName, (Float) value);
                break;
            case "Double":
                query = realm.where(clazz).equalTo(fieldName, (Double) value);
                break;
            case "Boolean":
                query = realm.where(clazz).equalTo(fieldName, (Boolean) value);
                break;
            case "Date":
                query = realm.where(clazz).equalTo(fieldName, (Date) value);
                break;
            case "byte[]":
                query = realm.where(clazz).equalTo(fieldName, (byte[]) value);
                break;
            default:
                throw new AssertionError("unknown type: " + value.getClass().getSimpleName());
        }

        assertEquals(expectedCount, query.count());
    }

    /**
     * Replaces the current thread executor with a another one for testing.
     * WARNING: This method should only be called before any async tasks have been started.
     *          Call {@link #resetRealmThreadExecutor()} before test return to reset the excutor to default.
     *
     * @param executor {@link RealmThreadPoolExecutor} that should replace the current one
     */
    public static RealmThreadPoolExecutor replaceRealmThreadExecutor(RealmThreadPoolExecutor executor)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = BaseRealm.class.getDeclaredField("asyncTaskExecutor");
        field.setAccessible(true);
        RealmThreadPoolExecutor oldExecutor = (RealmThreadPoolExecutor) field.get(null);
        field.set(field, executor);
        return oldExecutor;
    }

    /**
     * This will first wait for finishing all tasks in BaseRealm.asyncTaskExecutor, throws if time out.
     * Then reset the BaseRealm.asyncTaskExecutor to the default value.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void resetRealmThreadExecutor() throws NoSuchFieldException, IllegalAccessException {
        waitRealmThreadExecutorFinish();
        replaceRealmThreadExecutor(RealmThreadPoolExecutor.newDefaultExecutor());
    }

    /**
     * Waits and checks if all tasks in BaseRealm.asyncTaskExecutor can be finished in 5 seconds, otherwise fails the test.
     */
    public static void waitRealmThreadExecutorFinish() {
        int counter = 50;
        while (counter > 0) {
            if (BaseRealm.asyncTaskExecutor.getActiveCount() == 0) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
            counter--;
        }
        fail("'BaseRealm.asyncTaskExecutor' is not finished in " + counter/10.0D + " seconds");
    }

    /**
     * Emulates an environment where RxJava is not available.
     *
     * @param config {@link RealmConfiguration} instance to be modified.
     */
    public static void emulateRxJavaUnavailable(RealmConfiguration config) {
        //noinspection TryWithIdenticalCatches
        try {
            final Field field = config.getClass().getDeclaredField("rxObservableFactory");
            field.setAccessible(true);
            field.set(config, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class Task {
        public abstract void run() throws Exception;
    }

    public static void executeOnNonLooperThread(final Task task) throws Throwable {
        final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable e) {
                    thrown.set(e);
                    if (e instanceof Error) {
                        throw (Error) e;
                    }
                }
            }
        };
        thread.start();
        thread.join();

        final Throwable throwable = thrown.get();
        if (throwable != null) {
            throw throwable;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteRecursively(f);
            }
        }

        if (!file.delete()) {
            throw new AssertionError("failed to delete " + file.getAbsolutePath());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isSelinuxEnforcing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // SELinux is not enabled for these versions.
            return false;
        }
        try {
            final Process process = new ProcessBuilder("/system/bin/getenforce").start();
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    return reader.readLine().toLowerCase(Locale.ENGLISH).equals("enforcing");
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            } finally {
                try {
                    process.waitFor();
                } catch (InterruptedException ignored) {
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static void populateLinkedDataSet(Realm realm) {
        realm.beginTransaction();
        realm.delete(BacklinksSource.class);
        realm.delete(BacklinksTarget.class);

        BacklinksTarget target1 = realm.createObject(BacklinksTarget.class);
        target1.setId(1);

        BacklinksTarget target2 = realm.createObject(BacklinksTarget.class);
        target2.setId(2);

        BacklinksTarget target3 = realm.createObject(BacklinksTarget.class);
        target3.setId(3);

        BacklinksSource source1 = realm.createObject(BacklinksSource.class);
        source1.setName("1");
        source1.setChild(target1);
        BacklinksSource source2 = realm.createObject(BacklinksSource.class);
        source2.setName("2");
        source2.setChild(target2);

        BacklinksSource source3 = realm.createObject(BacklinksSource.class);
        source3.setName("3");

        BacklinksSource source4 = realm.createObject(BacklinksSource.class);
        source4.setName("4");
        source4.setChild(target1);

        realm.commitTransaction();
    }

    /**
     * This method will kill all tasks then shutdown and replace the SyncManager.NETWORK_POOL_EXECUTOR
     * with a fresh and empty instance. This should only be called when exiting tests.
     *
     * If the build does not support Sync, this method will do nothing
     */
    private static final Field networkPoolExecutorField;
    static {
        Class syncManager = null;
        try {
            syncManager = Class.forName("io.realm.SyncManager");
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        try {
            networkPoolExecutorField = (syncManager != null) ? syncManager.getDeclaredField("NETWORK_POOL_EXECUTOR") : null;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Could not find field: NETWORK_POOL_EXECUTOR\n" + Util.getStackTrace(e));
        }
    }

    public static void waitForNetworkThreadExecutorToFinish() {
        if (networkPoolExecutorField == null) {
            return; // This build do not support Sync
        }
        try {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) networkPoolExecutorField.get(null);
            // Since this method should only be called when exiting a test, it should be safe to just
            // cancel all ongoing network requests and shut down the pool as soon as possible.
            // When shut down we replace it with a new, now empty, pool that can be used by future
            // tests
            pool.shutdownNow();
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new AssertionError("NetworkPoolExecutor was not shut down in time:\n" + Util.getStackTrace(e));
            } finally {
                // Replace the executor, since the old one is now dead.
                // The setup of this should mirror what is done in SyncManager.
                networkPoolExecutorField.set(null, new ThreadPoolExecutor(
                        10, 10, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100)));
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(Util.getStackTrace(e));
        }
    }

}
