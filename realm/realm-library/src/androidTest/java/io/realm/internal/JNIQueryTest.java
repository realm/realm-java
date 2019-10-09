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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@Ignore("__CORE6__: using index should be deprecated, use ColKey, ObjKey")
public class JNIQueryTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;
    private Table table;
    private final long[] oneNullTable = new long[]{NativeObject.NULLPTR};


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
        long colKey2 = table.getColumnKey("name");

        long cnt = query.equalTo(new long[]{colKey2}, oneNullTable, "D").count();
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
        try { table.where().equalTo(new long[]{0}, oneNullTable, 1).or().validateQuery();       fail("missing a second filter"); }      catch (UnsupportedOperationException ignore) {}
        try { table.where().or().validateQuery();                                 fail("just an or()"); }                 catch (UnsupportedOperationException ignore) {}
        try { table.where().group().equalTo(new long[]{0}, oneNullTable, 1).validateQuery();    fail("missing a closing group"); }      catch (UnsupportedOperationException ignore) {}

        try { table.where().group().count();                                fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().validateQuery();                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().find();                                 fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().minimumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().maximumInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().sumInt(0);                              fail(); }                               catch (UnsupportedOperationException ignore) {}
        try { table.where().group().averageInt(0);                          fail(); }                               catch (UnsupportedOperationException ignore) {}

        try { table.where().endGroup().equalTo(new long[]{0}, oneNullTable, 1).validateQuery(); fail("ends group, no start"); }         catch (UnsupportedOperationException ignore) {}
        try { table.where().equalTo(new long[]{0}, oneNullTable, 1).endGroup().validateQuery(); fail("ends group, no start"); }         catch (UnsupportedOperationException ignore) {}

        try { table.where().equalTo(new long[]{0}, oneNullTable, 1).endGroup().find();    fail("ends group, no start"); }         catch (UnsupportedOperationException ignore) {}
    }

    @Test
    public void invalidColumnIndexEqualTo() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Boolean
        try { query.equalTo(new long[]{-1}, oneNullTable, true); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, true);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, true); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Date
        try { query.equalTo(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.equalTo(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, 4.5d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.equalTo(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, 1.4f);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.equalTo(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, 1);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String
        try { query.equalTo(new long[]{-1}, oneNullTable, "a"); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, "a");  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, "a"); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String case true
        try { query.equalTo(new long[]{-1}, oneNullTable, "a", Case.SENSITIVE); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, "a", Case.SENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, "a", Case.SENSITIVE); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String case false
        try { query.equalTo(new long[]{-1}, oneNullTable, "a", Case.INSENSITIVE); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{9}, oneNullTable, "a", Case.INSENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.equalTo(new long[]{10}, oneNullTable, "a", Case.INSENSITIVE); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexNotEqualTo() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();


        // Date
        try { query.notEqualTo(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.notEqualTo(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, 4.5d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.notEqualTo(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, 1.4f);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.notEqualTo(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, 1);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String
        try { query.notEqualTo(new long[]{-1}, oneNullTable, "a"); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, "a");  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, "a"); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String case true
        try { query.notEqualTo(new long[]{-1}, oneNullTable, "a", Case.SENSITIVE); fail("-1column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, "a", Case.SENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, "a", Case.SENSITIVE); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String case false
        try { query.notEqualTo(new long[]{-1}, oneNullTable, "a", Case.INSENSITIVE); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{9}, oneNullTable, "a", Case.INSENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{10}, oneNullTable, "a", Case.INSENSITIVE); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexGreaterThan() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Date
        try { query.greaterThan(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{9}, oneNullTable, new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.greaterThan(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{9}, oneNullTable, 4.5d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.greaterThan(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{9}, oneNullTable, 1.4f);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.greaterThan(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{9}, oneNullTable, 1);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.greaterThan(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexGreaterThanOrEqual() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Date
        try { query.greaterThanOrEqual(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{9}, oneNullTable, new Date()); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.greaterThanOrEqual(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{9}, oneNullTable, 4.5d); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.greaterThanOrEqual(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{9}, oneNullTable, 1.4f); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.greaterThanOrEqual(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{9}, oneNullTable, 1); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexLessThan() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Date
        try { query.lessThan(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{9}, oneNullTable, new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.lessThan(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{9}, oneNullTable, 4.5d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.lessThan(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{9}, oneNullTable, 1.4f);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.lessThan(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{9}, oneNullTable, 1);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThan(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexLessThanOrEqual() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Date
        try { query.lessThanOrEqual(new long[]{-1}, oneNullTable, new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{9}, oneNullTable, new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{10}, oneNullTable, new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.lessThanOrEqual(new long[]{-1}, oneNullTable, 4.5d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{9}, oneNullTable, 4.5d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{10}, oneNullTable, 4.5d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.lessThanOrEqual(new long[]{-1}, oneNullTable, 1.4f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{9}, oneNullTable, 1.4f); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{10}, oneNullTable, 1.4f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.lessThanOrEqual(new long[]{-1}, oneNullTable, 1); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{9}, oneNullTable, 1); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{10}, oneNullTable, 1); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexBetween() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // Date
        try { query.between(new long[]{-1}, new Date(), new Date()); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.between(new long[]{9}, new Date(), new Date());  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.between(new long[]{10}, new Date(), new Date()); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Double
        try { query.between(new long[]{-1}, 4.5d, 6.0d); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.between(new long[]{9}, 4.5d, 6.0d);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.between(new long[]{10}, 4.5d, 6.0d); fail("10 column index"); } catch (RuntimeException ignore) {}


        // Float
        try { query.between(new long[]{-1}, 1.4f, 5.8f); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.between(new long[]{9}, 1.4f, 5.8f); fail("9 column index"); }   catch (RuntimeException ignore) {}
        try { query.between(new long[]{10}, 1.4f, 5.8f); fail("10 column index"); } catch (RuntimeException ignore) {}

        // Int / long
        try { query.between(new long[]{-1}, 1, 10); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.between(new long[]{9}, 1, 10);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.between(new long[]{10}, 1, 10); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @Test
    public void invalidColumnIndexContains() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        TableQuery query = table.where();

        // String
        try { query.contains(new long[]{-1}, oneNullTable, "hey"); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.contains(new long[]{9}, oneNullTable, "hey");  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.contains(new long[]{10}, oneNullTable, "hey"); fail("10 column index"); } catch (RuntimeException ignore) {}

        // String case true
        try { query.contains(new long[]{-1}, oneNullTable, "hey", Case.SENSITIVE); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.contains(new long[]{9}, oneNullTable, "hey", Case.SENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.contains(new long[]{10}, oneNullTable, "hey", Case.SENSITIVE); fail("-0 column index"); } catch (RuntimeException ignore) {}

        // String case false
        try { query.contains(new long[]{-1}, oneNullTable, "hey", Case.INSENSITIVE); fail("-1 column index"); } catch (RuntimeException ignore) {}
        try { query.contains(new long[]{9}, oneNullTable, "hey", Case.INSENSITIVE);  fail("9 column index"); }  catch (RuntimeException ignore) {}
        try { query.contains(new long[]{10}, oneNullTable, "hey", Case.INSENSITIVE); fail("10 column index"); } catch (RuntimeException ignore) {}
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void nullInputQuery() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.DATE, "dateCol");
                t.addColumn(RealmFieldType.STRING, "stringCol");
            }
        });

        Date nullDate = null;
        try { t.where().equalTo(new long[]{0}, oneNullTable, nullDate);               fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().notEqualTo(new long[]{0}, oneNullTable, nullDate);            fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().greaterThan(new long[]{0}, oneNullTable, nullDate);           fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().greaterThanOrEqual(new long[]{0}, oneNullTable, nullDate);    fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().lessThan(new long[]{0}, oneNullTable, nullDate);              fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().lessThanOrEqual(new long[]{0}, oneNullTable, nullDate);       fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().between(new long[]{0}, nullDate, new Date());   fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().between(new long[]{0}, new Date(), nullDate);   fail("Date is null"); } catch (IllegalArgumentException ignore) { }
        try { t.where().between(new long[]{0}, nullDate, nullDate);     fail("Dates are null"); } catch (IllegalArgumentException ignore) { }

        String nullString = null;
        try { t.where().equalTo(new long[]{1}, oneNullTable, nullString);                         fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().equalTo(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);       fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().notEqualTo(new long[]{1}, oneNullTable, nullString);                      fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().notEqualTo(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);    fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().contains(new long[]{1}, oneNullTable, nullString);                        fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().contains(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);      fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().beginsWith(new long[]{1}, oneNullTable, nullString);                      fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().beginsWith(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);    fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().endsWith(new long[]{1}, oneNullTable, nullString);                        fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().endsWith(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);      fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().like(new long[]{1}, oneNullTable, nullString);                            fail("String is null"); } catch (IllegalStateException ignore) { }
        try { t.where().like(new long[]{1}, oneNullTable, nullString, Case.INSENSITIVE);          fail("String is null"); } catch (IllegalStateException ignore) { }
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

        TableQuery query = table.where().greaterThan(new long[]{table.getColumnKey("score")}, oneNullTable, 600);

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


        sharedRealm.beginTransaction();
        TestHelper.addRowWithValues(t, new long[]{columnKey1, columnKey2, columnKey3, columnKey4, columnKey5, columnKey6, columnKey7},
                new Object[]{new byte[]{1,2,3}, true, new Date(1384423149761L), 4.5d, 5.7f, 100, "string"});
        sharedRealm.commitTransaction();

        TableQuery q = t.where().greaterThan(new long[]{columnKey6}, oneNullTable, 1000); // No matches

        assertEquals(-1, q.find());
    }

    @Test
    public void queryWithWrongDataType() {

        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        long columnKey1 = table.getColumnKey("binary");
        long columnKey2 = table.getColumnKey("boolean");
        long columnKey3 = table.getColumnKey("date");
        long columnKey4 = table.getColumnKey("double");
        long columnKey5 = table.getColumnKey("float");
        long columnKey6 = table.getColumnKey("long");
        long columnKey7 = table.getColumnKey("string");
        long[] columnKeys = new long[]{columnKey1, columnKey2, columnKey3, columnKey4, columnKey5, columnKey6, columnKey7};

        // Queries the table.
        TableQuery query = table.where();

        // Compares strings in non string columns.
        for (int i = 0; i < 6; i++) {
                try { query.equalTo(new long[]{columnKeys[i]}, oneNullTable, "string");    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(new long[]{columnKeys[i]}, oneNullTable, "string"); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.beginsWith(new long[]{columnKeys[i]}, oneNullTable, "string"); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.endsWith(new long[]{columnKeys[i]}, oneNullTable, "string");   fail(); } catch(IllegalArgumentException ignore) {}
                try { query.like(new long[]{columnKeys[i]}, oneNullTable, "string");       fail(); } catch(IllegalArgumentException ignore) {}
                try { query.contains(new long[]{columnKeys[i]}, oneNullTable, "string");   fail(); } catch(IllegalArgumentException ignore) {}
        }


        // Compares integer in non integer columns.
        for (int i = 0; i <= 6; i++) {
            if (i != 5) {
                try { query.equalTo(new long[]{columnKeys[i]}, oneNullTable, 123);            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(new long[]{columnKeys[i]}, oneNullTable, 123);         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(new long[]{columnKeys[i]}, oneNullTable, 123);           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123);    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(new long[]{columnKeys[i]}, oneNullTable, 123);        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(new long[]{columnKeys[i]}, 123, 321);                     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares float in non float columns.
        for (int i = 0; i <= 6; i++) {
            if (i != 4) {
                try { query.equalTo(new long[]{columnKeys[i]}, oneNullTable, 123F);            fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(new long[]{columnKeys[i]}, oneNullTable, 123F);         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(new long[]{columnKeys[i]}, oneNullTable, 123F);           fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123F);    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(new long[]{columnKeys[i]}, oneNullTable, 123F);        fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123F); fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(new long[]{columnKeys[i]}, 123F, 321F);                    fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares double in non double columns.
        for (int i = 0; i <= 6; i++) {
            if (i != 3) {
                try { query.equalTo(new long[]{columnKeys[i]}, oneNullTable, 123D);                     fail(); } catch(IllegalArgumentException ignore) {}
                try { query.notEqualTo(new long[]{columnKeys[i]}, oneNullTable, 123D);                  fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(new long[]{columnKeys[i]}, oneNullTable, 123D);                    fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123D);             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(new long[]{columnKeys[i]}, oneNullTable, 123D);                 fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(new long[]{columnKeys[i]}, oneNullTable, 123D);          fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(new long[]{columnKeys[i]}, 123D, 321D);                             fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares boolean in non boolean columns.
        for (int i = 0; i <= 6; i++) {
            if (i != 1) {
              try { query.equalTo(new long[]{columnKeys[i]}, oneNullTable, true);                       fail(); } catch(IllegalArgumentException ignore) {}
            }
        }

        // Compares date.
        /* TODO:
        for (int i = 0; i <= 8; i++) {
            if (i != 2) {
                try { query.equal(i, new Date());                   fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThan(i, new Date());                fail(); } catch(IllegalArgumentException ignore) {}
                try { query.lessThanOrEqual(i, new Date());         fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThan(i, new Date());             fail(); } catch(IllegalArgumentException ignore) {}
                try { query.greaterThanOrEqual(i, new Date());      fail(); } catch(IllegalArgumentException ignore) {}
                try { query.between(i, new Date(), new Date());     fail(); } catch(IllegalArgumentException ignore) {}
            }
        }
        */
    }

    @Test
    public void columnIndexOutOfBounds() {
        Table table = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        long columnKey1 = table.getColumnKey("binary");
        long columnKey2 = table.getColumnKey("boolean");
        long columnKey3 = table.getColumnKey("date");
        long columnKey7 = table.getColumnKey("string");

        // Queries the table.
        TableQuery query = table.where();

        try { query.minimumInt(columnKey1);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumFloat(columnKey1);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumDouble(columnKey1);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumInt(columnKey2);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumFloat(columnKey2);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumDouble(columnKey2);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumInt(columnKey3);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumFloat(columnKey3);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumDouble(columnKey3);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumInt(columnKey7);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumFloat(columnKey7);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.minimumDouble(columnKey7);           fail(); } catch(IllegalArgumentException ignore) {}

        try { query.maximumInt(columnKey1);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumFloat(columnKey1);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumDouble(columnKey1);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumInt(columnKey2);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumFloat(columnKey2);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumDouble(columnKey2);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumInt(columnKey3);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumFloat(columnKey3);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumDouble(columnKey3);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumInt(columnKey7);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumFloat(columnKey7);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.maximumDouble(columnKey7);           fail(); } catch(IllegalArgumentException ignore) {}

        try { query.sumInt(columnKey1);                     fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumFloat(columnKey1);                fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumDouble(columnKey1);               fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumInt(columnKey2);                     fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumFloat(columnKey2);                fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumDouble(columnKey2);               fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumInt(columnKey3);                     fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumFloat(columnKey3);                fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumDouble(columnKey3);               fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumInt(columnKey7);                     fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumFloat(columnKey7);                fail(); } catch(IllegalArgumentException ignore) {}
        try { query.sumDouble(columnKey7);               fail(); } catch(IllegalArgumentException ignore) {}

        try { query.averageInt(columnKey1);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageFloat(columnKey1);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageDouble(columnKey1);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageInt(columnKey2);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageFloat(columnKey2);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageDouble(columnKey2);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageInt(columnKey3);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageFloat(columnKey3);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageDouble(columnKey3);           fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageInt(columnKey7);                 fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageFloat(columnKey7);            fail(); } catch(IllegalArgumentException ignore) {}
        try { query.averageDouble(columnKey7);           fail(); } catch(IllegalArgumentException ignore) {}
        // Out of bounds for string
        try { query.equalTo(new long[]{7}, oneNullTable, "string");                 fail(); } catch(RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{7}, oneNullTable, "string");              fail(); } catch(RuntimeException ignore) {}
        try { query.beginsWith(new long[]{7}, oneNullTable, "string");              fail(); } catch(RuntimeException ignore) {}
        try { query.endsWith(new long[]{7}, oneNullTable, "string");                fail(); } catch(RuntimeException ignore) {}
        try { query.like(new long[]{7}, oneNullTable, "string");                    fail(); } catch(RuntimeException ignore) {}
        try { query.contains(new long[]{7}, oneNullTable, "string");                fail(); } catch(RuntimeException ignore) {}


        // Out of bounds for integer
        try { query.equalTo(new long[]{7}, oneNullTable, 123);                      fail(); } catch(RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{7}, oneNullTable, 123);                   fail(); } catch(RuntimeException ignore) {}
        try { query.lessThan(new long[]{7}, oneNullTable, 123);                     fail(); } catch(RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{7}, oneNullTable, 123);              fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThan(new long[]{7}, oneNullTable, 123);                  fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{7}, oneNullTable, 123);           fail(); } catch(RuntimeException ignore) {}
        try { query.between(new long[]{7}, 123, 321);                               fail(); } catch(RuntimeException ignore) {}


        // Out of bounds for float
        try { query.equalTo(new long[]{7}, oneNullTable, 123F);                     fail(); } catch(RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{7}, oneNullTable, 123F);                  fail(); } catch(RuntimeException ignore) {}
        try { query.lessThan(new long[]{7}, oneNullTable, 123F);                    fail(); } catch(RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{7}, oneNullTable, 123F);             fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThan(new long[]{7}, oneNullTable, 123F);                 fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{7}, oneNullTable, 123F);          fail(); } catch(RuntimeException ignore) {}
        try { query.between(new long[]{7}, 123F, 321F);                             fail(); } catch(RuntimeException ignore) {}


        // Out of bounds for double
        try { query.equalTo(new long[]{7}, oneNullTable, 123D);                     fail(); } catch(RuntimeException ignore) {}
        try { query.notEqualTo(new long[]{7}, oneNullTable, 123D);                  fail(); } catch(RuntimeException ignore) {}
        try { query.lessThan(new long[]{7}, oneNullTable, 123D);                    fail(); } catch(RuntimeException ignore) {}
        try { query.lessThanOrEqual(new long[]{7}, oneNullTable, 123D);             fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThan(new long[]{7}, oneNullTable, 123D);                 fail(); } catch(RuntimeException ignore) {}
        try { query.greaterThanOrEqual(new long[]{7}, oneNullTable, 123D);          fail(); } catch(RuntimeException ignore) {}
        try { query.between(new long[]{7}, 123D, 321D);                             fail(); } catch(RuntimeException ignore) {}


        // Out of bounds for boolean
        try { query.equalTo(new long[]{7}, oneNullTable, true);                     fail(); } catch(RuntimeException ignore) {}
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

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, distantPast).count());
        assertEquals(6L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, distantPast).count());
        assertEquals(0L, table.where().lessThan(new long[]{columnKey.get()}, oneNullTable, distantPast).count());
        assertEquals(1L, table.where().lessThanOrEqual(new long[]{columnKey.get()}, oneNullTable, distantPast).count());
        assertEquals(6L, table.where().greaterThan(new long[]{columnKey.get()}, oneNullTable, distantPast).count());
        assertEquals(7L, table.where().greaterThanOrEqual(new long[]{columnKey.get()}, oneNullTable, distantPast).count());

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, past).count());
        assertEquals(6L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, past).count());
        assertEquals(1L, table.where().lessThan(new long[]{columnKey.get()}, oneNullTable, past).count());
        assertEquals(2L, table.where().lessThanOrEqual(new long[]{columnKey.get()}, oneNullTable, past).count());
        assertEquals(5L, table.where().greaterThan(new long[]{columnKey.get()}, oneNullTable, past).count());
        assertEquals(6L, table.where().greaterThanOrEqual(new long[]{columnKey.get()}, oneNullTable, past).count());

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());
        assertEquals(6L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());
        assertEquals(2L, table.where().lessThan(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());
        assertEquals(3L, table.where().lessThanOrEqual(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());
        assertEquals(4L, table.where().greaterThan(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());
        assertEquals(5L, table.where().greaterThanOrEqual(new long[]{columnKey.get()}, oneNullTable, new Date(0)).count());

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, future).count());
        assertEquals(6L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, future).count());
        assertEquals(5L, table.where().lessThan(new long[]{columnKey.get()}, oneNullTable, future).count());
        assertEquals(6L, table.where().lessThanOrEqual(new long[]{columnKey.get()}, oneNullTable, future).count());
        assertEquals(1L, table.where().greaterThan(new long[]{columnKey.get()}, oneNullTable, future).count());
        assertEquals(2L, table.where().greaterThanOrEqual(new long[]{columnKey.get()}, oneNullTable, future).count());

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());
        assertEquals(6L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());
        assertEquals(6L, table.where().lessThan(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());
        assertEquals(7L, table.where().lessThanOrEqual(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());
        assertEquals(0L, table.where().greaterThan(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());
        assertEquals(1L, table.where().greaterThanOrEqual(new long[]{columnKey.get()}, oneNullTable, distantFuture).count());

        // between

        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, distantPast, distantPast).count());
        assertEquals(2L, table.where().between(new long[]{columnKey.get()}, distantPast, past).count());
        assertEquals(3L, table.where().between(new long[]{columnKey.get()}, distantPast, new Date(0)).count());
        assertEquals(5L, table.where().between(new long[]{columnKey.get()}, distantPast, new Date(10000)).count());
        assertEquals(6L, table.where().between(new long[]{columnKey.get()}, distantPast, future).count());
        assertEquals(7L, table.where().between(new long[]{columnKey.get()}, distantPast, distantFuture).count());

        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, past, distantPast).count());
        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, past, past).count());
        assertEquals(2L, table.where().between(new long[]{columnKey.get()}, past, new Date(0)).count());
        assertEquals(4L, table.where().between(new long[]{columnKey.get()}, past, new Date(10000)).count());
        assertEquals(5L, table.where().between(new long[]{columnKey.get()}, past, future).count());
        assertEquals(6L, table.where().between(new long[]{columnKey.get()}, past, distantFuture).count());

        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, new Date(0), distantPast).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, new Date(0), past).count());
        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, new Date(0), new Date(0)).count());
        assertEquals(3L, table.where().between(new long[]{columnKey.get()}, new Date(0), new Date(10000)).count());
        assertEquals(4L, table.where().between(new long[]{columnKey.get()}, new Date(0), future).count());
        assertEquals(5L, table.where().between(new long[]{columnKey.get()}, new Date(0), distantFuture).count());

        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, new Date(10000), distantPast).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, new Date(10000), past).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, new Date(10000), new Date(0)).count());
        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, new Date(10000), new Date(10000)).count());
        assertEquals(2L, table.where().between(new long[]{columnKey.get()}, new Date(10000), future).count());
        assertEquals(3L, table.where().between(new long[]{columnKey.get()}, new Date(10000), distantFuture).count());

        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, future, distantPast).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, future, past).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, future, new Date(0)).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, future, new Date(10000)).count());
        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, future, future).count());
        assertEquals(2L, table.where().between(new long[]{columnKey.get()}, future, distantFuture).count());

        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, distantFuture, distantPast).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, distantFuture, past).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, distantFuture, new Date(0)).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, distantFuture, new Date(10000)).count());
        assertEquals(0L, table.where().between(new long[]{columnKey.get()}, distantFuture, future).count());
        assertEquals(1L, table.where().between(new long[]{columnKey.get()}, distantFuture, distantFuture).count());
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

        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, binary1).count());
        assertEquals(1L, table.where().equalTo(new long[]{columnKey.get()}, oneNullTable, binary3).count());

        // Not equal to

        assertEquals(3L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, binary2).count());
        assertEquals(3L, table.where().notEqualTo(new long[]{columnKey.get()}, oneNullTable, binary4).count());
    }
}
