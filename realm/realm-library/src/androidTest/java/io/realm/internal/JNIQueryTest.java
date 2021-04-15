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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.realm.Mixed;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.TestRealmConfigurationFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
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

        long cnt = query.equalTo(null,"name", Mixed.valueOf("D")).count();
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
        try { table.where().equalTo(null, "", Mixed.valueOf(1)).or().validateQuery();       fail("missing a second filter"); }      catch (IllegalArgumentException ignore) {}
        try { table.where().beginGroup().equalTo(null,"", Mixed.valueOf(1)).validateQuery();    fail("missing a closing group"); }      catch (IllegalArgumentException ignore) {}

        try { table.where().beginGroup().count();                                fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().validateQuery();                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().find();                                 fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().minimumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().maximumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().sumInt(0);                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().beginGroup().averageInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}

        try { table.where().endGroup().equalTo(null,"", Mixed.valueOf(1)).validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
        try { table.where().equalTo(null,"", Mixed.valueOf(1)).endGroup().validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}

        try { table.where().equalTo(null,"", Mixed.valueOf(1)).endGroup().find();    fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
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

        TableQuery query = table.where().greaterThan(null,"score", Mixed.valueOf(600));

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

        TableQuery q = t.where().greaterThan(null,"long", Mixed.valueOf(1000)); // No matches

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
                try { query.equalTo(null, columnKeys[i], L123).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(null, columnKeys[i], L123).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(null, columnKeys[i], L123).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(null, columnKeys[i], L123).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(null, columnKeys[i], L123).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(null, columnKeys[i], L123).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(null, columnKeys[i], L123, L321).find();                     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares float in non float columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(null, columnKeys[i], F123).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(null, columnKeys[i], F123).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(null, columnKeys[i], F123).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(null, columnKeys[i], F123).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(null, columnKeys[i], F123).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(null, columnKeys[i], F123).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(null, columnKeys[i], F123, F321).find();                    fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares double in non double columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(null, columnKeys[i], D123).find();                     fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(null, columnKeys[i], D123).find();                  fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(null, columnKeys[i], D123).find();                    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(null, columnKeys[i], D123).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(null, columnKeys[i], D123).find();                 fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(null, columnKeys[i], D123).find();          fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(null, columnKeys[i], D123, D321).find();                             fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares boolean in non boolean columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
              try { query.equalTo(null, columnKeys[i], Mixed.valueOf(true)).find();                       fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares date.
        for (int i = 0; i <= 6; i++) {
            if (i != 2) {
                try { query.equalTo(null, columnKeys[i], date).find();                   fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(null, columnKeys[i], date).find();                fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(null, columnKeys[i], date).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(null, columnKeys[i], date).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(null, columnKeys[i], date).find();      fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(null, columnKeys[i], date, date).find();                     fail(); } catch(IllegalArgumentException ignore) {}
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

        assertEquals(1L, table.where().equalTo(null, "date", distantPast).count());
        assertEquals(6L, table.where().notEqualTo(null, "date", distantPast).count());
        assertEquals(0L, table.where().lessThan(null, "date", distantPast).count());
        assertEquals(1L, table.where().lessThanOrEqual(null, "date", distantPast).count());
        assertEquals(6L, table.where().greaterThan(null, "date", distantPast).count());
        assertEquals(7L, table.where().greaterThanOrEqual(null, "date", distantPast).count());

        assertEquals(1L, table.where().equalTo(null, "date", past).count());
        assertEquals(6L, table.where().notEqualTo(null, "date", past).count());
        assertEquals(1L, table.where().lessThan(null, "date", past).count());
        assertEquals(2L, table.where().lessThanOrEqual(null, "date", past).count());
        assertEquals(5L, table.where().greaterThan(null, "date", past).count());
        assertEquals(6L, table.where().greaterThanOrEqual(null, "date", past).count());

        assertEquals(1L, table.where().equalTo(null, "date", date0).count());
        assertEquals(6L, table.where().notEqualTo(null, "date", date0).count());
        assertEquals(2L, table.where().lessThan(null, "date", date0).count());
        assertEquals(3L, table.where().lessThanOrEqual(null, "date", date0).count());
        assertEquals(4L, table.where().greaterThan(null, "date", date0).count());
        assertEquals(5L, table.where().greaterThanOrEqual(null, "date", date0).count());

        assertEquals(1L, table.where().equalTo(null, "date", future).count());
        assertEquals(6L, table.where().notEqualTo(null, "date", future).count());
        assertEquals(5L, table.where().lessThan(null, "date", future).count());
        assertEquals(6L, table.where().lessThanOrEqual(null, "date", future).count());
        assertEquals(1L, table.where().greaterThan(null, "date", future).count());
        assertEquals(2L, table.where().greaterThanOrEqual(null, "date", future).count());

        assertEquals(1L, table.where().equalTo(null, "date", distantFuture).count());
        assertEquals(6L, table.where().notEqualTo(null, "date", distantFuture).count());
        assertEquals(6L, table.where().lessThan(null, "date", distantFuture).count());
        assertEquals(7L, table.where().lessThanOrEqual(null, "date", distantFuture).count());
        assertEquals(0L, table.where().greaterThan(null, "date", distantFuture).count());
        assertEquals(1L, table.where().greaterThanOrEqual(null, "date", distantFuture).count());

        // between

        assertEquals(1L, table.where().between(null, "date", distantPast, distantPast).count());
        assertEquals(2L, table.where().between(null, "date", distantPast, past).count());
        assertEquals(3L, table.where().between(null, "date", distantPast, date0).count());
        assertEquals(5L, table.where().between(null, "date", distantPast, date10000).count());
        assertEquals(6L, table.where().between(null, "date", distantPast, future).count());
        assertEquals(7L, table.where().between(null, "date", distantPast, distantFuture).count());

        assertEquals(0L, table.where().between(null, "date", past, distantPast).count());
        assertEquals(1L, table.where().between(null, "date", past, past).count());
        assertEquals(2L, table.where().between(null, "date", past, date0).count());
        assertEquals(4L, table.where().between(null, "date", past, date10000).count());
        assertEquals(5L, table.where().between(null, "date", past, future).count());
        assertEquals(6L, table.where().between(null, "date", past, distantFuture).count());

        assertEquals(0L, table.where().between(null, "date", date0, distantPast).count());
        assertEquals(0L, table.where().between(null, "date", date0, past).count());
        assertEquals(1L, table.where().between(null, "date", date0, date0).count());
        assertEquals(3L, table.where().between(null, "date", date0, date10000).count());
        assertEquals(4L, table.where().between(null, "date", date0, future).count());
        assertEquals(5L, table.where().between(null, "date", date0, distantFuture).count());

        assertEquals(0L, table.where().between(null, "date", date10000, distantPast).count());
        assertEquals(0L, table.where().between(null, "date", date10000, past).count());
        assertEquals(0L, table.where().between(null, "date", date10000, date0).count());
        assertEquals(1L, table.where().between(null, "date", date10000, date10000).count());
        assertEquals(2L, table.where().between(null, "date", date10000, future).count());
        assertEquals(3L, table.where().between(null, "date", date10000, distantFuture).count());

        assertEquals(0L, table.where().between(null, "date", future, distantPast).count());
        assertEquals(0L, table.where().between(null, "date", future, past).count());
        assertEquals(0L, table.where().between(null, "date", future, date0).count());
        assertEquals(0L, table.where().between(null, "date", future, date10000).count());
        assertEquals(1L, table.where().between(null, "date", future, future).count());
        assertEquals(2L, table.where().between(null, "date", future, distantFuture).count());

        assertEquals(0L, table.where().between(null, "date", distantFuture, distantPast).count());
        assertEquals(0L, table.where().between(null, "date", distantFuture, past).count());
        assertEquals(0L, table.where().between(null, "date", distantFuture, date0).count());
        assertEquals(0L, table.where().between(null, "date", distantFuture, date10000).count());
        assertEquals(0L, table.where().between(null, "date", distantFuture, future).count());
        assertEquals(1L, table.where().between(null, "date", distantFuture, distantFuture).count());
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

        assertEquals(1L, table.where().equalTo(null, "binary", binary1).count());
        assertEquals(1L, table.where().equalTo(null, "binary", binary3).count());

        // Not equal to

        assertEquals(3L, table.where().notEqualTo(null, "binary", binary2).count());
        assertEquals(3L, table.where().notEqualTo(null, "binary", binary4).count());
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

        assertEquals(1L, table.where().equalTo(null, "decimal128", one).count());
        assertEquals(2L, table.where().notEqualTo(null, "decimal128", one).count());
        assertEquals(0L, table.where().lessThan(null, "decimal128", one).count());
        assertEquals(1L, table.where().lessThanOrEqual(null, "decimal128", one).count());
        assertEquals(2L, table.where().greaterThan(null, "decimal128", one).count());
        assertEquals(3L, table.where().greaterThanOrEqual(null, "decimal128", one).count());

        assertEquals(1L, table.where().equalTo(null, "decimal128", two).count());
        assertEquals(2L, table.where().notEqualTo(null, "decimal128", two).count());
        assertEquals(1L, table.where().lessThan(null, "decimal128", two).count());
        assertEquals(2L, table.where().lessThanOrEqual(null, "decimal128", two).count());
        assertEquals(1L, table.where().greaterThan(null, "decimal128", two).count());
        assertEquals(2L, table.where().greaterThanOrEqual(null, "decimal128", two).count());

        assertEquals(1L, table.where().equalTo(null, "decimal128", three).count());
        assertEquals(2L, table.where().notEqualTo(null, "decimal128", three).count());
        assertEquals(2L, table.where().lessThan(null, "decimal128", three).count());
        assertEquals(3L, table.where().lessThanOrEqual(null, "decimal128", three).count());
        assertEquals(0L, table.where().greaterThan(null, "decimal128", three).count());
        assertEquals(1L, table.where().greaterThanOrEqual(null, "decimal128", three).count());
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

        assertEquals(1L, table.where().equalTo(null, "objectid", one).count());
        assertEquals(2L, table.where().notEqualTo(null, "objectid", one).count());
        assertEquals(0L, table.where().lessThan(null, "objectid", one).count());
        assertEquals(1L, table.where().lessThanOrEqual(null, "objectid", one).count());
        assertEquals(2L, table.where().greaterThan(null, "objectid", one).count());
        assertEquals(3L, table.where().greaterThanOrEqual(null, "objectid", one).count());

        assertEquals(1L, table.where().equalTo(null, "objectid", two).count());
        assertEquals(2L, table.where().notEqualTo(null, "objectid", two).count());
        assertEquals(1L, table.where().lessThan(null, "objectid", two).count());
        assertEquals(2L, table.where().lessThanOrEqual(null, "objectid", two).count());
        assertEquals(1L, table.where().greaterThan(null, "objectid", two).count());
        assertEquals(2L, table.where().greaterThanOrEqual(null, "objectid", two).count());

        assertEquals(1L, table.where().equalTo(null, "objectid", three).count());
        assertEquals(2L, table.where().notEqualTo(null, "objectid", three).count());
        assertEquals(2L, table.where().lessThan(null, "objectid", three).count());
        assertEquals(3L, table.where().lessThanOrEqual(null, "objectid", three).count());
        assertEquals(0L, table.where().greaterThan(null, "objectid", three).count());
        assertEquals(1L, table.where().greaterThanOrEqual(null, "objectid", three).count());
    }
}
