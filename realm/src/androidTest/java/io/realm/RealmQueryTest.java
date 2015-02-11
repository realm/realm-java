package io.realm;

import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;

public class RealmQueryTest extends AndroidTestCase{

    protected final static int TEST_DATA_SIZE = 10;

    protected Realm testRealm;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_LONG_KOREAN_CHAR = "델타";
    private final static String FIELD_LONG_GREEK_CHAR = "Δέλτα";
    private final static String FIELD_FLOAT_KOREAN_CHAR = "베타";
    private final static String FIELD_FLOAT_GREEK_CHAR = "βήτα";

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null)
            testRealm.close();
    }

    private void populateTestRealm(int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }

    public void testRealmQueryBetween() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .between(FIELD_LONG, 0, 9).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data ").findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .between(FIELD_LONG, 2, 20).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).between(FIELD_LONG, 2, 20)
                .beginsWith(FIELD_STRING, "test data 1").findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryGreaterThan() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .greaterThan(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .greaterThan(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.234567f);
        resultList = query.between(FIELD_LONG, 1, 20).findAll();
        assertEquals(10, resultList.size());
    }


    public void testRealmQueryGreaterThanOrEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .greaterThanOrEqualTo(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 9, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith(FIELD_STRING, "test data 1")
                .greaterThanOrEqualTo(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class)
                .greaterThanOrEqualTo(FIELD_FLOAT, 11.234567f);
        query = query.between(FIELD_LONG, 1, 20);

        resultList = query.beginsWith(FIELD_STRING, "test data 15").findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryOr() {
        populateTestRealm(200);

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.or().between(FIELD_LONG, 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    public void testRealmQueryNot() {
        populateTestRealm(); // create TEST_DATA_SIZE objects

        // only one object with value 5 -> TEST_DATA_SIZE-1 object with value "not 5"
        RealmResults<AllTypes> list1 = testRealm.where(AllTypes.class).not().equalTo(FIELD_LONG, 5).findAll();
        assertEquals(TEST_DATA_SIZE - 1, list1.size());

        // not().greater() and lessThenOrEqual() must be the same
        RealmResults<AllTypes> list2 = testRealm.where(AllTypes.class).not().greaterThan(FIELD_LONG, 5).findAll();
        RealmResults<AllTypes> list3 = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_LONG, 5).findAll();
        assertEquals(list2.size(), list3.size());
        for (int i = 0; i < list2.size(); i++) {
            assertEquals(list2.get(i).getColumnLong(), list3.get(i).getColumnLong());
        }

        // excepted result: 0, 1, 2, 5
        long expected[] = {0, 1, 2, 5};
        RealmResults<AllTypes> list4 = testRealm.where(AllTypes.class)
                .equalTo(FIELD_LONG, 5)
                .or()
                .not().beginGroup()
                    .greaterThan(FIELD_LONG, 2)
                 .endGroup()
                .findAll();
        assertEquals(4, list4.size());
        for (int i = 0; i < list4.size(); i++) {
            assertEquals(expected[i], list4.get(i).getColumnLong());
        }
    }

    public void testRealmQueryNotFailure() {
        // a not() alone must fail
        try {
            RealmResults<AllTypes> list = testRealm.where(AllTypes.class).not().findAll();
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    public void testRealmQueryImplicitAnd() {
        populateTestRealm(200);

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults<AllTypes> resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 81.234567f);
        resultList = query.between(FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryLessThan() {
        populateTestRealm(200);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).
                lessThan(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).lessThan(FIELD_FLOAT, 31.234567f);
        resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryLessThanOrEqual() {
        populateTestRealm(200);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .lessThanOrEqualTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(31, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo(FIELD_FLOAT, 31.234567f)
                .between(FIELD_LONG, 11, 20).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryEqualTo() {
        populateTestRealm(200);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .equalTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryEqualToNonLatinCharacters() {
        populateTestRealm(200);

        RealmResults<NonLatinFieldNames> resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 1).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_GREEK_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryNotEqualTo() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .notEqualTo(FIELD_LONG, 31).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitive() {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT);

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "DaTa 0", RealmQuery.CASE_INSENSITIVE)
                .or().contains("columnString", "20")
                .findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "DATA").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "TEST", RealmQuery.CASE_INSENSITIVE).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters() {
        populateTestRealm();

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("Αλφα");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("βήτα");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("δέλτα");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .contains("columnString", "Α", RealmQuery.CASE_INSENSITIVE)
                .or().contains("columnString", "δ")
                .findAll();
        // Without case sensitive there is 3, Α = α
        // assertEquals(3,resultList.size());
        assertEquals(2, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "α").findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ",
                RealmQuery.CASE_INSENSITIVE).findAll();
        // Without case sensitive there is 1, Δ = δ
        // assertEquals(1,resultList.size());
        assertEquals(0, resultList.size());
    }

    public void testQueryWithNonExistingField() {
        try {
            testRealm.where(AllTypes.class).equalTo("NotAField", 13).findAll();
            fail("Should throw exception");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testRealmQueryLink() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        testRealm.commitTransaction();

        // Dog.weight has index 4 which is more than the total number of columns in Owner
        // This tests exposes a subtle error where the Owner tablespec is used instead of Dog tablespec.
        RealmResults<Dog> dogs = testRealm.where(Owner.class).findFirst().getDogs().where()
                .findAllSorted("name", RealmResults.SORT_ORDER_ASCENDING);
        Dog dog = dogs.where().equalTo("weight", 1d).findFirst();
        assertEquals(dog1, dog);
    }


    public void testSortMultiFailures() {
        // zero fields specified
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{}, new boolean[]{});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // number of fields and sorting orders don't match
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING},
                            new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // null is not allowed
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class).findAllSorted(null, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class).findAllSorted(new String[]{FIELD_STRING},
                    null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        // non-existing field name
        try {
            RealmResults<AllTypes> results = testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING, "dont-exist"},
                            new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    public void testSortSingleField() {
        testRealm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        testRealm.commitTransaction();

        RealmResults<AllTypes> sortedList = testRealm.where(AllTypes.class)
                .findAllSorted(new String[]{FIELD_LONG}, new boolean[]{RealmResults.SORT_ORDER_DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    public void testSubqueryScope() {
        populateTestRealm();
        RealmResults<AllTypes> result = testRealm.where(AllTypes.class).lessThan("columnLong", 5).findAll();
        RealmResults<AllTypes> subQueryResult = result.where().greaterThan("columnLong", 3).findAll();
        assertEquals(1, subQueryResult.size());
    }
}
