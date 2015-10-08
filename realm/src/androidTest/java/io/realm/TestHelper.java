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
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import io.realm.internal.log.Logger;

import static junit.framework.Assert.fail;

import io.realm.entities.NullTypes;
import io.realm.entities.StringOnly;
import io.realm.internal.Table;

public class TestHelper {

    public static String streamToString(InputStream in) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(in));
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
        return new ByteArrayInputStream(str.getBytes(Charset.forName("UTF-8")));
    }

    // Copies a Realm file from assets to app files dir
    public static void copyRealmFromAssets(Context context, String realmPath, String newName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(realmPath);
        File file = new File(context.getFilesDir(), newName);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buf)) > -1) {
            outputStream.write(buf, 0, bytesRead);
        }
        outputStream.close();
        is.close();
    }

    // Creates a simple migration step in order to support null
    // FIXME: generate a new encrypted.realm will null support
    public static RealmMigration prepareMigrationToNullSupportStep() {
        RealmMigration realmMigration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table stringOnly = realm.getTable(StringOnly.class);
                stringOnly.convertColumnToNullable(stringOnly.getColumnIndex("chars"));

                return 0;
            }
        };
        return realmMigration;
    }


    // Deletes the old database and copies a new one into its place
    public static void prepareDatabaseFromAssets(Context context, String realmPath, String newName) throws IOException {
        Realm.deleteRealm(createConfiguration(context, newName));
        TestHelper.copyRealmFromAssets(context, realmPath, newName);
    }

    // Returns a random key used by encrypted Realms.
    public static byte[] getRandomKey() {
        byte[] key = new byte[64];
        new Random().nextBytes(key);
        return key;
    }

    // Returns a random key from the given seed. Used by encrypted Realms.
    public static byte[] getRandomKey(long seed) {
        byte[] key = new byte[64];
        new Random(seed).nextBytes(key);
        return key;
    }

    /**
     * Returns a Logger that will fail if it is asked to log a message above a certain level.
     *
     * @param failureLevel {@link Log} level from which the unit test will fail.
     * @return Logger implementation
     */
    public static Logger getFailureLogger(final int failureLevel) {
        return new Logger() {

            private void failIfEqualOrAbove(int logLevel, int failureLevel) {
                if (logLevel >= failureLevel) {
                    fail("Message logged that was above valid level: " + logLevel + " >= " + failureLevel);
                }
            }

            @Override
            public void v(String message) {
                failIfEqualOrAbove(Log.VERBOSE, failureLevel);
            }

            @Override
            public void v(String message, Throwable t) {
                failIfEqualOrAbove(Log.VERBOSE, failureLevel);
            }

            @Override
            public void d(String message) {
                failIfEqualOrAbove(Log.DEBUG, failureLevel);
            }

            @Override
            public void d(String message, Throwable t) {
                failIfEqualOrAbove(Log.DEBUG, failureLevel);
            }

            @Override
            public void i(String message) {
                failIfEqualOrAbove(Log.INFO, failureLevel);
            }

            @Override
            public void i(String message, Throwable t) {
                failIfEqualOrAbove(Log.INFO, failureLevel);
            }

            @Override
            public void w(String message) {
                failIfEqualOrAbove(Log.WARN, failureLevel);
            }

            @Override
            public void w(String message, Throwable t) {
                failIfEqualOrAbove(Log.WARN, failureLevel);
            }

            @Override
            public void e(String message) {
                failIfEqualOrAbove(Log.ERROR, failureLevel);
            }

            @Override
            public void e(String message, Throwable t) {
                failIfEqualOrAbove(Log.ERROR, failureLevel);
            }
        };
    }

    public static String getRandomString(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) r.nextInt(128));
        }
        return sb.toString();
    }

    public static class StubInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return 0; // Stub implementation
        }
    }

    // Alloc as much garbage as we can. Pass maxSize = 0 to use it.
    public static byte[] allocGarbage(int garbageSize) {
        if (garbageSize == 0) {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            garbageSize = (int)(maxMemory - totalMemory)/10*9;
        }
        byte garbage[] = new byte[0];
        try {
            if (garbageSize > 0) {
                garbage = new byte[garbageSize];
                garbage[0] = 1;
                garbage[garbage.length - 1] = 1;
            }
        } catch (OutOfMemoryError oom) {
            return allocGarbage(garbageSize/10*9);
        }

        return garbage;
    }

    // Creates SHA512 hash of a String. Can be used as password for encrypted Realms.
    public static byte[] SHA512(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(str.getBytes("UTF-8"), 0, str.length());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static RealmConfiguration createConfiguration(Context context) {
        return createConfiguration(context, Realm.DEFAULT_REALM_NAME);
    }

    public static RealmConfiguration createConfiguration(Context context, String name) {
        return createConfiguration(context.getFilesDir(), name);
    }

    public static RealmConfiguration createConfiguration(File folder, String name) {
        return createConfiguration(folder, name, null);
    }

    public static RealmConfiguration createConfiguration(Context context, String name, byte[] key) {
        return createConfiguration(context.getFilesDir(), name, key);
    }

    public static RealmConfiguration createConfiguration(File dir, String name, byte[] key) {
        RealmConfiguration.Builder config = new RealmConfiguration.Builder(dir).name(name);
        if (key != null) {
            config.encryptionKey(key);
        }

        return config.build();
    }

    public static void populateTestRealmForNullTests(Realm testRealm) {

        // +-+--------+------+---------+--------------------+
        // | | string | link | numeric | numeric (not null) |
        // +-+--------+------+---------+--------------------+
        // |0| Fish   |    0 |       1 |                  1 |
        // |1| null   | null |    null |                  0 |
        // |2| Horse  |    1 |       3 |                  3 |
        // +-+--------+------+---------+--------------------+

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
    public static void initNullTypesTableExcludes(Realm realm, String excludingField) {
        Table table = realm.getTable(NullTypes.class);
        if (!excludingField.equals("id")) {
            table.addColumn(RealmFieldType.INTEGER, "id", Table.NOT_NULLABLE);
            table.addSearchIndex(table.getColumnIndex("id"));
            table.setPrimaryKey("id");
        }
        if (!excludingField.equals("fieldStringNotNull")) {
            table.addColumn(RealmFieldType.STRING, "fieldStringNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldStringNull")) {
            table.addColumn(RealmFieldType.STRING, "fieldStringNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldBytesNotNull")) {
            table.addColumn(RealmFieldType.BINARY, "fieldBytesNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldBytesNull")) {
            table.addColumn(RealmFieldType.BINARY, "fieldBytesNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldBooleanNotNull")) {
            table.addColumn(RealmFieldType.BOOLEAN, "fieldBooleanNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldBooleanNull")) {
            table.addColumn(RealmFieldType.BOOLEAN, "fieldBooleanNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldByteNotNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldByteNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldByteNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldByteNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldShortNotNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldShortNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldShortNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldShortNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldIntegerNotNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldIntegerNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldIntegerNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldIntegerNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldLongNotNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldLongNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldLongNull")) {
            table.addColumn(RealmFieldType.INTEGER, "fieldLongNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldFloatNotNull")) {
            table.addColumn(RealmFieldType.FLOAT, "fieldFloatNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldFloatNull")) {
            table.addColumn(RealmFieldType.FLOAT, "fieldFloatNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldDoubleNotNull")) {
            table.addColumn(RealmFieldType.DOUBLE, "fieldDoubleNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldDoubleNull")) {
            table.addColumn(RealmFieldType.DOUBLE, "fieldDoubleNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldDateNotNull")) {
            table.addColumn(RealmFieldType.DATE, "fieldDateNotNull", Table.NOT_NULLABLE);
        }
        if (!excludingField.equals("fieldDateNull")) {
            table.addColumn(RealmFieldType.DATE, "fieldDateNull", Table.NULLABLE);
        }
        if (!excludingField.equals("fieldObjectNull")) {
            table.addColumnLink(RealmFieldType.OBJECT, "fieldObjectNull", table);
        }
    }

}
