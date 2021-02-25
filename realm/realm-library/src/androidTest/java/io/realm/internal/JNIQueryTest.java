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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

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

        long cnt = query.equalTo("name", "D").count();
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
        try { table.where().equalTo("", 1).or().validateQuery();       fail("missing a second filter"); }      catch (IllegalArgumentException ignore) {}
        try { table.where().group().equalTo("", 1).validateQuery();    fail("missing a closing group"); }      catch (IllegalArgumentException ignore) {}

        try { table.where().group().count();                                fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().validateQuery();                              fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().find();                                 fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().minimumInt(0);                          fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().maximumInt(0);                          fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().sumInt(0);                              fail(); }                               catch (IllegalArgumentException ignore) {}
        try { table.where().group().averageInt(0);                          fail(); }                               catch (IllegalArgumentException ignore) {}

        try { table.where().endGroup().equalTo("", 1).validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
        try { table.where().equalTo("", 1).endGroup().validateQuery(); fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}

        try { table.where().equalTo("", 1).endGroup().find();    fail("ends group, no start"); }         catch (IllegalArgumentException ignore) {}
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

        TableQuery query = table.where().greaterThan("score", 600);

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

        TableQuery q = t.where().greaterThan("long", 1000); // No matches

        assertEquals(-1, q.find());
    }

    @Test
    public void queryWithWrongDataType() {

        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        String[] columnKeys = new String[]{"binary", "boolean", "date", "double", "float", "long", "string"};

        // Queries the table.
        TableQuery query = table.where();

        // Compares integer in non integer columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], 123).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], 123).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], 123).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], 123).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], 123).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], 123).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], 123, 321).find();                     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares float in non float columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], 123.5F).find();            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], 123.5F).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], 123.5F).find();           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], 123.5F).find();    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], 123.5F).find();        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], 123.5F).find(); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], 123.5F, 321.5F).find();                    fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares double in non double columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
                try { query.equalTo(columnKeys[i], 123.5D).find();                     fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(columnKeys[i], 123.5D).find();                  fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], 123.5D).find();                    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], 123.5D).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], 123.5D).find();                 fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], 123.5D).find();          fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], 123.5D, 321.5D).find();                             fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares boolean in non boolean columns.
        for (int i = 0; i <= 6; i++) {
            if ((i != 5) && (i != 1) && (i != 3) && (i != 4)) {
              try { query.equalTo(columnKeys[i], true).find();                       fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares date.
        for (int i = 0; i <= 6; i++) {
            if (i != 2) {
                try { query.equalTo(columnKeys[i], new Date()).find();                   fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(columnKeys[i], new Date()).find();                fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(columnKeys[i], new Date()).find();         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(columnKeys[i], new Date()).find();             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(columnKeys[i], new Date()).find();      fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(columnKeys[i], new Date(), new Date()).find();                     fail(); } catch(IllegalArgumentException ignore) {}
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

        final Date past = new Date(TimeUnit.SECONDS.toMillis(Integer.MIN_VALUE - 100L));
        final Date future = new Date(TimeUnit.SECONDS.toMillis(Integer.MAX_VALUE + 1L));
        final Date distantPast = new Date(Long.MIN_VALUE);
        final Date distantFuture = new Date(Long.MAX_VALUE);

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DATE, "date"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(10000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(0)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{new Date(1000)});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{future});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{distantFuture});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{past});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{distantPast});
            }
        });

        assertEquals(1L, table.where().equalTo("date", distantPast).count());
        assertEquals(6L, table.where().notEqualTo("date", distantPast).count());
        assertEquals(0L, table.where().lessThan("date", distantPast).count());
        assertEquals(1L, table.where().lessThanOrEqual("date", distantPast).count());
        assertEquals(6L, table.where().greaterThan("date", distantPast).count());
        assertEquals(7L, table.where().greaterThanOrEqual("date", distantPast).count());

        assertEquals(1L, table.where().equalTo("date", past).count());
        assertEquals(6L, table.where().notEqualTo("date", past).count());
        assertEquals(1L, table.where().lessThan("date", past).count());
        assertEquals(2L, table.where().lessThanOrEqual("date", past).count());
        assertEquals(5L, table.where().greaterThan("date", past).count());
        assertEquals(6L, table.where().greaterThanOrEqual("date", past).count());

        assertEquals(1L, table.where().equalTo("date", new Date(0)).count());
        assertEquals(6L, table.where().notEqualTo("date", new Date(0)).count());
        assertEquals(2L, table.where().lessThan("date", new Date(0)).count());
        assertEquals(3L, table.where().lessThanOrEqual("date", new Date(0)).count());
        assertEquals(4L, table.where().greaterThan("date", new Date(0)).count());
        assertEquals(5L, table.where().greaterThanOrEqual("date", new Date(0)).count());

        assertEquals(1L, table.where().equalTo("date", future).count());
        assertEquals(6L, table.where().notEqualTo("date", future).count());
        assertEquals(5L, table.where().lessThan("date", future).count());
        assertEquals(6L, table.where().lessThanOrEqual("date", future).count());
        assertEquals(1L, table.where().greaterThan("date", future).count());
        assertEquals(2L, table.where().greaterThanOrEqual("date", future).count());

        assertEquals(1L, table.where().equalTo("date", distantFuture).count());
        assertEquals(6L, table.where().notEqualTo("date", distantFuture).count());
        assertEquals(6L, table.where().lessThan("date", distantFuture).count());
        assertEquals(7L, table.where().lessThanOrEqual("date", distantFuture).count());
        assertEquals(0L, table.where().greaterThan("date", distantFuture).count());
        assertEquals(1L, table.where().greaterThanOrEqual("date", distantFuture).count());

        // between

        assertEquals(1L, table.where().between("date", distantPast, distantPast).count());
        assertEquals(2L, table.where().between("date", distantPast, past).count());
        assertEquals(3L, table.where().between("date", distantPast, new Date(0)).count());
        assertEquals(5L, table.where().between("date", distantPast, new Date(10000)).count());
        assertEquals(6L, table.where().between("date", distantPast, future).count());
        assertEquals(7L, table.where().between("date", distantPast, distantFuture).count());

        assertEquals(0L, table.where().between("date", past, distantPast).count());
        assertEquals(1L, table.where().between("date", past, past).count());
        assertEquals(2L, table.where().between("date", past, new Date(0)).count());
        assertEquals(4L, table.where().between("date", past, new Date(10000)).count());
        assertEquals(5L, table.where().between("date", past, future).count());
        assertEquals(6L, table.where().between("date", past, distantFuture).count());

        assertEquals(0L, table.where().between("date", new Date(0), distantPast).count());
        assertEquals(0L, table.where().between("date", new Date(0), past).count());
        assertEquals(1L, table.where().between("date", new Date(0), new Date(0)).count());
        assertEquals(3L, table.where().between("date", new Date(0), new Date(10000)).count());
        assertEquals(4L, table.where().between("date", new Date(0), future).count());
        assertEquals(5L, table.where().between("date", new Date(0), distantFuture).count());

        assertEquals(0L, table.where().between("date", new Date(10000), distantPast).count());
        assertEquals(0L, table.where().between("date", new Date(10000), past).count());
        assertEquals(0L, table.where().between("date", new Date(10000), new Date(0)).count());
        assertEquals(1L, table.where().between("date", new Date(10000), new Date(10000)).count());
        assertEquals(2L, table.where().between("date", new Date(10000), future).count());
        assertEquals(3L, table.where().between("date", new Date(10000), distantFuture).count());

        assertEquals(0L, table.where().between("date", future, distantPast).count());
        assertEquals(0L, table.where().between("date", future, past).count());
        assertEquals(0L, table.where().between("date", future, new Date(0)).count());
        assertEquals(0L, table.where().between("date", future, new Date(10000)).count());
        assertEquals(1L, table.where().between("date", future, future).count());
        assertEquals(2L, table.where().between("date", future, distantFuture).count());

        assertEquals(0L, table.where().between("date", distantFuture, distantPast).count());
        assertEquals(0L, table.where().between("date", distantFuture, past).count());
        assertEquals(0L, table.where().between("date", distantFuture, new Date(0)).count());
        assertEquals(0L, table.where().between("date", distantFuture, new Date(10000)).count());
        assertEquals(0L, table.where().between("date", distantFuture, future).count());
        assertEquals(1L, table.where().between("date", distantFuture, distantFuture).count());
    }

    @Test
    public void byteArrayQuery() throws Exception {

        final byte[] binary1 = new byte[] {0x01, 0x02, 0x03, 0x04};
        final byte[] binary2 = new byte[] {0x05, 0x02, 0x03, 0x08};
        final byte[] binary3 = new byte[] {0x09, 0x0a, 0x0b, 0x04};
        final byte[] binary4 = new byte[] {0x05, 0x0a, 0x0b, 0x10};

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.BINARY, "binary"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary1});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary2});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary3});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{(Object) binary4});
            }
        });

        // Equal to

        assertEquals(1L, table.where().equalTo("binary", binary1).count());
        assertEquals(1L, table.where().equalTo("binary", binary3).count());

        // Not equal to

        assertEquals(3L, table.where().notEqualTo("binary", binary2).count());
        assertEquals(3L, table.where().notEqualTo("binary", binary4).count());
    }

    @Test
    public void decimal128Query() throws Exception {
        final Decimal128 one = new Decimal128(1);
        final Decimal128 two = new Decimal128(2);
        final Decimal128 three = new Decimal128(3);

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.DECIMAL128, "decimal128"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{one});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{two});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{three});
            }
        });

        assertEquals(1L, table.where().equalTo("decimal128", one).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", one).count());
        assertEquals(0L, table.where().lessThan("decimal128", one).count());
        assertEquals(1L, table.where().lessThanOrEqual("decimal128", one).count());
        assertEquals(2L, table.where().greaterThan("decimal128", one).count());
        assertEquals(3L, table.where().greaterThanOrEqual("decimal128", one).count());

        assertEquals(1L, table.where().equalTo("decimal128", two).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", two).count());
        assertEquals(1L, table.where().lessThan("decimal128", two).count());
        assertEquals(2L, table.where().lessThanOrEqual("decimal128", two).count());
        assertEquals(1L, table.where().greaterThan("decimal128", two).count());
        assertEquals(2L, table.where().greaterThanOrEqual("decimal128", two).count());

        assertEquals(1L, table.where().equalTo("decimal128", three).count());
        assertEquals(2L, table.where().notEqualTo("decimal128", three).count());
        assertEquals(2L, table.where().lessThan("decimal128", three).count());
        assertEquals(3L, table.where().lessThanOrEqual("decimal128", three).count());
        assertEquals(0L, table.where().greaterThan("decimal128", three).count());
        assertEquals(1L, table.where().greaterThanOrEqual("decimal128", three).count());
    }

    @Test
    public void objectIdQuery() throws Exception {
        final ObjectId one = new ObjectId(new Date(10));
        final ObjectId two = new ObjectId(new Date(20));
        final ObjectId three = new ObjectId(new Date(30));

        final AtomicLong columnKey = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey.set(table.addColumn(RealmFieldType.OBJECT_ID, "objectid"));

                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{one});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{two});
                TestHelper.addRowWithValues(table, new long[]{columnKey.get()}, new Object[]{three});
            }
        });

        assertEquals(1L, table.where().equalTo("objectid", one).count());
        assertEquals(2L, table.where().notEqualTo("objectid", one).count());
        assertEquals(0L, table.where().lessThan("objectid", one).count());
        assertEquals(1L, table.where().lessThanOrEqual("objectid", one).count());
        assertEquals(2L, table.where().greaterThan("objectid", one).count());
        assertEquals(3L, table.where().greaterThanOrEqual("objectid", one).count());

        assertEquals(1L, table.where().equalTo("objectid", two).count());
        assertEquals(2L, table.where().notEqualTo("objectid", two).count());
        assertEquals(1L, table.where().lessThan("objectid", two).count());
        assertEquals(2L, table.where().lessThanOrEqual("objectid", two).count());
        assertEquals(1L, table.where().greaterThan("objectid", two).count());
        assertEquals(2L, table.where().greaterThanOrEqual("objectid", two).count());

        assertEquals(1L, table.where().equalTo("objectid", three).count());
        assertEquals(2L, table.where().notEqualTo("objectid", three).count());
        assertEquals(2L, table.where().lessThan("objectid", three).count());
        assertEquals(3L, table.where().lessThanOrEqual("objectid", three).count());
        assertEquals(0L, table.where().greaterThan("objectid", three).count());
        assertEquals(1L, table.where().greaterThanOrEqual("objectid", three).count());
    }
}
