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
import android.util.Log;

import org.junit.Assert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.NullTypes;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.StringOnly;
import io.realm.internal.Collection;
import io.realm.internal.Table;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.LogLevel;
import io.realm.log.RealmLogger;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class TestHelper {

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
        if (o instanceof Boolean)
            return RealmFieldType.BOOLEAN;
        if (o instanceof String)
            return RealmFieldType.STRING;
        if (o instanceof Long)
            return RealmFieldType.INTEGER;
        if (o instanceof Float)
            return RealmFieldType.FLOAT;
        if (o instanceof Double)
            return RealmFieldType.DOUBLE;
        if (o instanceof Date)
            return RealmFieldType.DATE;
        if (o instanceof byte[])
            return RealmFieldType.BINARY;
        return RealmFieldType.UNSUPPORTED_MIXED;
    }

    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns.
     *
     * @return
     */
    public static Table getTableWithAllColumnTypes() {
        Table t = new Table();

        t.addColumn(RealmFieldType.BINARY, "binary");
        t.addColumn(RealmFieldType.BOOLEAN, "boolean");
        t.addColumn(RealmFieldType.DATE, "date");
        t.addColumn(RealmFieldType.DOUBLE, "double");
        t.addColumn(RealmFieldType.FLOAT, "float");
        t.addColumn(RealmFieldType.INTEGER, "long");
        t.addColumn(RealmFieldType.STRING, "string");

        return t;
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

    // Creates a simple migration step in order to support null.
    // FIXME: generate a new encrypted.realm will null support
    public static RealmMigration prepareMigrationToNullSupportStep() {
        RealmMigration realmMigration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                Table stringOnly = realm.schema.getTable(StringOnly.class);
                stringOnly.convertColumnToNullable(stringOnly.getColumnIndex("chars"));
            }
        };
        return realmMigration;
    }

    // Returns a random key used by encrypted Realms.
    public static byte[] getRandomKey() {
        byte[] key = new byte[64];
        RANDOM.nextBytes(key);
        return key;
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
     * @param failureLevel {@link Log} level from which the unit test will fail.
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

    public static String getRandomString(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) r.nextInt(128)); // Restrict to standard ASCII chars.
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
        for (int i = 0; i < words.length; i++) {
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

    public static void populateForMultiSort(Realm typedRealm) {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(typedRealm.getConfiguration());
        populateForMultiSort(dynamicRealm);
        dynamicRealm.close();
        typedRealm.waitForChange();
    }

    public static void populateForMultiSort(DynamicRealm realm) {
        realm.beginTransaction();
        realm.delete(AllTypes.CLASS_NAME);
        DynamicRealmObject object1 = realm.createObject(AllTypes.CLASS_NAME);
        object1.setLong(AllTypes.FIELD_LONG, 5);
        object1.setString(AllTypes.FIELD_STRING, "Adam");

        DynamicRealmObject object2 = realm.createObject(AllTypes.CLASS_NAME);
        object2.setLong(AllTypes.FIELD_LONG, 4);
        object2.setString(AllTypes.FIELD_STRING, "Brian");

        DynamicRealmObject object3 = realm.createObject(AllTypes.CLASS_NAME);
        object3.setLong(AllTypes.FIELD_LONG, 4);
        object3.setString(AllTypes.FIELD_STRING, "Adam");
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
        awaitOrFail(latch, 60);
    }

    public static void awaitOrFail(CountDownLatch latch, int numberOfSeconds) {
        try {
            if (android.os.Debug.isDebuggerConnected()) {
                // If we are debugging the tests, just waits without a timeout. In case we are stopping at a break point
                // and timeout happens.
                latch.await();
            } else if (!latch.await(numberOfSeconds, TimeUnit.SECONDS)) {
                fail("Test took longer than " + numberOfSeconds + " seconds");
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    // Cleans resource, shutdowns the executor service and throws any background exception.
    @SuppressWarnings("Finally")
    public static void exitOrThrow(final ExecutorService executorService,
                                   final CountDownLatch signalTestFinished,
                                   final CountDownLatch signalClosedRealm,
                                   final Looper[] looper,
                                   final Throwable[] throwable) throws Throwable {

        // Waits for the signal indicating the test's use case is done.
        try {
            // Even if this fails we want to try as hard as possible to cleanup. If we fail to close all resources
            // properly, the `after()` method will most likely throw as well because it tries do delete any Realms
            // used. Any exception in the `after()` code will mask the original error.
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            if (looper[0] != null) {
                // Failing to quit the looper will not execute the finally block responsible
                // of closing the Realm.
                looper[0].quit();
            }

            // Waits for the finally block to execute and closes the Realm.
            TestHelper.awaitOrFail(signalClosedRealm);
            // Closes the executor.
            // This needs to be called after waiting since it might interrupt waitRealmThreadExecutorFinish().
            executorService.shutdownNow();

            if (throwable[0] != null) {
                // Throws any assertion errors happened in the background thread.
                throw throwable[0];
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
     * @param collection a {@link Collection} instance.
     * @param tableClass a Class of Table.
     * @return a created {@link RealmResults} instance.
     */
    public static <T extends RealmObject> RealmResults<T> newRealmResults(
            BaseRealm realm, Collection collection, Class<T> tableClass) {
        //noinspection TryWithIdenticalCatches
        try {
            final Constructor<RealmResults> c = RealmResults.class.getDeclaredConstructor(
                    BaseRealm.class, Collection.class, Class.class);
            c.setAccessible(true);
            //noinspection unchecked
            return c.newInstance(realm, collection, tableClass);
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
        fail("'BaseRealm.asyncTaskExecutor' is not finished in " + counter/10 + " seconds");
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
}
