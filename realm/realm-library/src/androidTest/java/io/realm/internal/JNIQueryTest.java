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

package io.realm.internal;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Mixed;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.TestRealmConfigurationFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@Ignore("FIXME: See https://github.com/realm/realm-java/issues/7330")
public class JNIQueryTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() throws Exception {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    private void init() {
        table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                long colKey1 = table.addColumn(RealmFieldType.INTEGER, "number");
                long colKey2 = table.addColumn(RealmFieldType.STRING, "name");

                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{10, "A"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{11, "B"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{12, "C"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{13, "B"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{14, "D"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{16, "D"});
            }
        });

        assertEquals(6, table.size());
    }

    @Test
    public void shouldQuery() {
        init();
        TableQuery query = table.where();

        long colKey1 = table.getColumnKey("number");

        long cnt = query.equalTo("name", Mixed.valueOf("D"), TableQuery.TypeFilter.STRING).count();
        assertEquals(2, cnt);

        cnt = query.minimumInt(colKey1);
        assertEquals(14, cnt);

        cnt = query.maximumInt(colKey1);
        assertEquals(16, cnt);

        cnt = query.sumInt(colKey1);
        assertEquals(14+16, cnt);

        double avg = query.averageInt(colKey1);
        assertEquals(15.0, avg, Double.MIN_NORMAL);

        // TODO: Add tests with all parameters
    }


    @Test
    public void nonCompleteQuery() {
        init();

        // All the following queries are not valid, e.g contain a group but not a closing group, an or() but not a second filter etc
        try { table.where().equalTo("", Mixed.valueOf(1), TableQuery.TypeFilter.INTEGER).or().validateQuery();       fail("missing a second filter"); }      catch (IllegalArgumentException ignore) {}
        try { table.where().beginGroup().equalTo("", Mixed.valueOf(1), TableQuery.TypeFilter.INTEGER).validateQuery();    fail("missing a closing group"); }      catch (IllegalArgumentException ignore) {}

        try { table.where().beginGroup().count();                                fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().validateQuery();                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().find();                                 fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().minimumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().maximumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().sumInt(0);                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().averageInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}

        try { table.where().endGroup().equalTo("", Mixed.valueOf(1), TableQuery.TypeFilter.INTEGER).validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
        try { table.where().equalTo("", Mixed.valueOf(1), TableQuery.TypeFilter.INTEGER).endGroup().validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}

        try { table.where().equalTo("", Mixed.valueOf(1), TableQuery.TypeFilter.INTEGER).endGroup().find();    fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
    }

    @Test
    public void shouldFind() {
        // Creates a table.
        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                long colKey1 = table.addColumn(RealmFieldType.STRING, "username");
                long colKey2 = table.addColumn(RealmFieldType.INTEGER, "score");
                long colKey3 = table.addColumn(RealmFieldType.BOOLEAN, "completed");

                // Inserts some values.
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Arnold", 420, false});    // 0
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Jane", 770, false});      // 1 *
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Erik", 600, false});      // 2
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Henry", 601, false});     // 3 *
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Bill", 564, true});       // 4
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2, colKey3}, new Object[]{"Janet", 875, false});     // 5 *
            }
        });

        TableQuery query = table.where().greaterThan("score", Mixed.valueOf(600), TableQuery.TypeFilter.INTEGER);

        // Finds first match.
        assertEquals(1, query.find());
    }

    @Test
    public void queryTestForNoMatches() {
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        long columnKey1 = t.getColumnKey("binary");
        long columnKey2 = t.getColumnKey("boolean");
        long columnKey3 = t.getColumnKey("date");
        long columnKey4 = t.getColumnKey("double");
        long columnKey5 = t.getColumnKey("float");
        long columnKey6 = t.getColumnKey("long");
        long columnKey7 = t.getColumnKey("string");
        long columnKey8 = t.getColumnKey("decimal128");
        long columnKey9 = t.getColumnKey("object_id");


        sharedRealm.beginTransaction();
        TestHelper.addRowWithValues(t, new long[]{columnKey1, columnKey2, columnKey3, columnKey4, columnKey5, columnKey6, columnKey7, columnKey8, columnKey9},
                new Object[]{new byte[]{1,2,3}, true, new Date(1384423149761L), 4.5d, 5.7f, 100, "string", new Decimal128(0), new ObjectId()});
        sharedRealm.commitTransaction();

        TableQuery q = t.where().greaterThan("long", Mixed.valueOf(1000), TableQuery.TypeFilter.INTEGER); // No matches

        assertEquals(-1, q.find());
    }

    @Test
    public void queryWithWrongDataType() {

        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        String[] columnKeys = new String[]{"binary", "boolean", "date", "double", "float", "long", "string"};

        // Queries the table.
        TableQuery query = table.where();

        Mixed L123 = Mixed.valueOf(123);
        Mixed L321 = Mixed.valueOf(321);
        Mixed F123 = Mixed.valueOf(123.5F);
        Mixed F321 = Mixed.valueOf(321.5F);
        Mixed D123 = Mixed.valueOf(123.5D);
        Mixed D321 = Mixed.valueOf(321.5D);
        Mixed date = Mixed.valueOf(new Date());

        // Compares integer in non integer columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], L123, TableQuery.TypeFilter.INTEGER).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], L123, L321, TableQuery.TypeFilter.INTEGER).find();                     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares float in non float columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], F123, TableQuery.TypeFilter.FLOAT).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], F123, F321, TableQuery.TypeFilter.FLOAT).find();                    fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares double in non double columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();                     fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();                  fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();                    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();                 fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], D123, TableQuery.TypeFilter.DOUBLE).find();          fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], D123, D321, TableQuery.TypeFilter.DOUBLE).find();                             fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares boolean in non boolean columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
              try { query.equalTo(columnKeys[i], Mixed.valueOf(true), TableQuery.TypeFilter.BOOLEAN).find();                       fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares date.
        for (int i = 0; i <= 6; i++) {
            if (i != 2) {
                try { query.equalTo(columnKeys[i], date, TableQuery.TypeFilter.DATE).find();                   fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], date, TableQuery.TypeFilter.DATE).find();                fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], date, TableQuery.TypeFilter.DATE).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], date, TableQuery.TypeFilter.DATE).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], date, TableQuery.TypeFilter.DATE).find();      fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], date, date, TableQuery.TypeFilter.DATE).find();                     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }
    }

    @Test
    public void maximumDate() {
        final AtomicLong columnKey = new AtomicLong(-1);
        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DATE, "date"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(0)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(10000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(1000)});
            }
        });

        assertEquals(new Date(10000), table.where().maximumDate(columnKey.get()));
    }

    @Test
    public void minimumDate() {
        final AtomicLong columnKey = new AtomicLong(-1);
        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DATE, "date"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(10000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(0)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(1000)});
            }
        });

        assertEquals(new Date(0), table.where().minimumDate(columnKey.get()));
    }

    @Test
    public void dateQuery() throws Exception {

        final Mixed past = Mixed.valueOf(new Date(TimeUnit.SECONDS.toMillis(Integer.MIN_VALUE - 100L)));
        final Mixed future = Mixed.valueOf(new Date(TimeUnit.SECONDS.toMillis(Integer.MAX_VALUE + 1L)));
        final Mixed distantPast = Mixed.valueOf(new Date(Long.MIN_VALUE));
        final Mixed distantFuture = Mixed.valueOf(new Date(Long.MAX_VALUE));
        final Mixed date0 = Mixed.valueOf(new Date(0));
        final Mixed date10000 = Mixed.valueOf(new Date(10000));

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DATE, "date"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(10000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(0)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(1000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{future.asDate()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{distantFuture.asDate()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{past.asDate()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{distantPast.asDate()});
            }
        });

        assertEquals(1L, table.where().equalTo("date", distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().notEqualTo("date", distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().lessThan("date", distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().lessThanOrEqual("date", distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().greaterThan("date", distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(7L, table.where().greaterThanOrEqual("date", distantPast, TableQuery.TypeFilter.DATE).count());

        assertEquals(1L, table.where().equalTo("date", past, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().notEqualTo("date", past, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().lessThan("date", past, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().lessThanOrEqual("date", past, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().greaterThan("date", past, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().greaterThanOrEqual("date", past, TableQuery.TypeFilter.DATE).count());

        assertEquals(1L, table.where().equalTo("date", date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().notEqualTo("date", date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().lessThan("date", date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(3L, table.where().lessThanOrEqual("date", date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(4L, table.where().greaterThan("date", date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().greaterThanOrEqual("date", date0, TableQuery.TypeFilter.DATE).count());

        assertEquals(1L, table.where().equalTo("date", future, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().notEqualTo("date", future, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().lessThan("date", future, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().lessThanOrEqual("date", future, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().greaterThan("date", future, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().greaterThanOrEqual("date", future, TableQuery.TypeFilter.DATE).count());

        assertEquals(1L, table.where().equalTo("date", distantFuture, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().notEqualTo("date", distantFuture, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().lessThan("date", distantFuture, TableQuery.TypeFilter.DATE).count());
        assertEquals(7L, table.where().lessThanOrEqual("date", distantFuture, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().greaterThan("date", distantFuture, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().greaterThanOrEqual("date", distantFuture, TableQuery.TypeFilter.DATE).count());

        // between

        assertEquals(1L, table.where().between("date", distantPast, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().between("date", distantPast, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(3L, table.where().between("date", distantPast, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().between("date", distantPast, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().between("date", distantPast, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(7L, table.where().between("date", distantPast, distantFuture, TableQuery.TypeFilter.DATE).count());

        assertEquals(0L, table.where().between("date", past, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().between("date", past, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().between("date", past, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(4L, table.where().between("date", past, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().between("date", past, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(6L, table.where().between("date", past, distantFuture, TableQuery.TypeFilter.DATE).count());

        assertEquals(0L, table.where().between("date", date0, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", date0, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().between("date", date0, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(3L, table.where().between("date", date0, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(4L, table.where().between("date", date0, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(5L, table.where().between("date", date0, distantFuture, TableQuery.TypeFilter.DATE).count());

        assertEquals(0L, table.where().between("date", date10000, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", date10000, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", date10000, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().between("date", date10000, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().between("date", date10000, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(3L, table.where().between("date", date10000, distantFuture, TableQuery.TypeFilter.DATE).count());

        assertEquals(0L, table.where().between("date", future, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", future, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", future, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", future, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().between("date", future, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(2L, table.where().between("date", future, distantFuture, TableQuery.TypeFilter.DATE).count());

        assertEquals(0L, table.where().between("date", distantFuture, distantPast, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", distantFuture, past, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", distantFuture, date0, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", distantFuture, date10000, TableQuery.TypeFilter.DATE).count());
        assertEquals(0L, table.where().between("date", distantFuture, future, TableQuery.TypeFilter.DATE).count());
        assertEquals(1L, table.where().between("date", distantFuture, distantFuture, TableQuery.TypeFilter.DATE).count());
    }

    @Test
    public void byteArrayQuery() throws Exception {

        final Mixed binary1 = Mixed.valueOf(new byte[] {0x01, 0x02, 0x03, 0x04});
        final Mixed binary2 = Mixed.valueOf(new byte[] {0x05, 0x02, 0x03, 0x08});
        final Mixed binary3 = Mixed.valueOf(new byte[] {0x09, 0x0a, 0x0b, 0x04});
        final Mixed binary4 = Mixed.valueOf(new byte[] {0x05, 0x0a, 0x0b, 0x10});

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.BINARY, "binary"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary1.asBinary()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary2.asBinary()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary3.asBinary()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary4.asBinary()});
            }
        });

        // Equal to

        assertEquals(1L, table.where().equalTo("binary", binary1, TableQuery.TypeFilter.BINARY).count());
        assertEquals(1L, table.where().equalTo("binary", binary3, TableQuery.TypeFilter.BINARY).count());

        // Not equal to

        assertEquals(3L, table.where().notEqualTo("binary", binary2, TableQuery.TypeFilter.BINARY).count());
        assertEquals(3L, table.where().notEqualTo("binary", binary4, TableQuery.TypeFilter.BINARY).count());
    }

    @Test
    public void decimal128Query() throws Exception {
        final Mixed one = Mixed.valueOf(new Decimal128(1));
        final Mixed two = Mixed.valueOf(new Decimal128(2));
        final Mixed three = Mixed.valueOf(new Decimal128(3));

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DECIMAL128, "decimal128"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{one.asDecimal128()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{two.asDecimal128()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{three.asDecimal128()});
            }
        });

        assertEquals(1L, table.where().equalTo("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(0L, table.where().lessThan("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(1L, table.where().lessThanOrEqual("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().greaterThan("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(3L, table.where().greaterThanOrEqual("decimal128", one, TableQuery.TypeFilter.DECIMAL128).count());

        assertEquals(1L, table.where().equalTo("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(1L, table.where().lessThan("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().lessThanOrEqual("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(1L, table.where().greaterThan("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().greaterThanOrEqual("decimal128", two, TableQuery.TypeFilter.DECIMAL128).count());

        assertEquals(1L, table.where().equalTo("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(2L, table.where().lessThan("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(3L, table.where().lessThanOrEqual("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(0L, table.where().greaterThan("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
        assertEquals(1L, table.where().greaterThanOrEqual("decimal128", three, TableQuery.TypeFilter.DECIMAL128).count());
    }

    @Test
    public void objectIdQuery() throws Exception {
        final Mixed one = Mixed.valueOf(new ObjectId(new Date(10)));
        final Mixed two = Mixed.valueOf(new ObjectId(new Date(20)));
        final Mixed three = Mixed.valueOf(new ObjectId(new Date(30)));

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.OBJECT_ID, "objectid"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{one.asObjectId()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{two.asObjectId()});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{three.asObjectId()});
            }
        });

        assertEquals(1L, table.where().equalTo("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().notEqualTo("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(0L, table.where().lessThan("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(1L, table.where().lessThanOrEqual("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().greaterThan("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(3L, table.where().greaterThanOrEqual("objectid", one, TableQuery.TypeFilter.OBJECT_ID).count());

        assertEquals(1L, table.where().equalTo("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().notEqualTo("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(1L, table.where().lessThan("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().lessThanOrEqual("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(1L, table.where().greaterThan("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().greaterThanOrEqual("objectid", two, TableQuery.TypeFilter.OBJECT_ID).count());

        assertEquals(1L, table.where().equalTo("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().notEqualTo("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(2L, table.where().lessThan("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(3L, table.where().lessThanOrEqual("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(0L, table.where().greaterThan("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
        assertEquals(1L, table.where().greaterThanOrEqual("objectid", three, TableQuery.TypeFilter.OBJECT_ID).count());
    }
}
