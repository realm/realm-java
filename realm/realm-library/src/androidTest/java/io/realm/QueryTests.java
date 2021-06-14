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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.UiThreadTestRule;

import javax.annotation.Nullable;

import io.realm.entities.AllJavaTypesUnsupportedTypes;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.rule.RunInLooperThread;


public abstract class QueryTests {
    public static final int TEST_DATA_SIZE = 10;
    public static final int TEST_NO_PRIMARY_KEY_NULL_TYPES_SIZE = 200;

    public static final long DECADE_MILLIS = 10 * TimeUnit.DAYS.toMillis(365);

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    protected static final List<RealmFieldType> SUPPORTED_IS_EMPTY_TYPES;
    protected static final List<RealmFieldType> NOT_SUPPORTED_IS_EMPTY_TYPES;
    protected static final List<RealmFieldType> SUPPORTED_IS_NOT_EMPTY_TYPES;
    protected static final List<RealmFieldType> NOT_SUPPORTED_IS_NOT_EMPTY_TYPES;

    static {
        ArrayList<RealmFieldType> list = new ArrayList<>(Arrays.asList(
                RealmFieldType.STRING,
                RealmFieldType.BINARY,
                RealmFieldType.LIST,
                RealmFieldType.OBJECT,
                RealmFieldType.INTEGER_LIST,
                RealmFieldType.BOOLEAN_LIST,
                RealmFieldType.STRING_LIST,
                RealmFieldType.BINARY_LIST,
                RealmFieldType.DATE_LIST,
                RealmFieldType.FLOAT_LIST,
                RealmFieldType.DOUBLE_LIST,
                RealmFieldType.DECIMAL128_LIST,
                RealmFieldType.OBJECT_ID_LIST,
                RealmFieldType.UUID_LIST,
                RealmFieldType.MIXED_LIST,
                RealmFieldType.LINKING_OBJECTS,
                RealmFieldType.STRING_TO_INTEGER_MAP,
                RealmFieldType.STRING_TO_BOOLEAN_MAP,
                RealmFieldType.STRING_TO_STRING_MAP,
                RealmFieldType.STRING_TO_BINARY_MAP,
                RealmFieldType.STRING_TO_DATE_MAP,
                RealmFieldType.STRING_TO_FLOAT_MAP,
                RealmFieldType.STRING_TO_DOUBLE_MAP,
                RealmFieldType.STRING_TO_DECIMAL128_MAP,
                RealmFieldType.STRING_TO_OBJECT_ID_MAP,
                RealmFieldType.STRING_TO_UUID_MAP,
                RealmFieldType.STRING_TO_MIXED_MAP,
                RealmFieldType.STRING_TO_LINK_MAP,
                RealmFieldType.INTEGER_SET,
                RealmFieldType.BOOLEAN_SET,
                RealmFieldType.STRING_SET,
                RealmFieldType.BINARY_SET,
                RealmFieldType.DATE_SET,
                RealmFieldType.FLOAT_SET,
                RealmFieldType.DOUBLE_SET,
                RealmFieldType.DECIMAL128_SET,
                RealmFieldType.OBJECT_ID_SET,
                RealmFieldType.UUID_SET,
                RealmFieldType.MIXED_SET,
                RealmFieldType.LINK_SET
        ));

        SUPPORTED_IS_EMPTY_TYPES = Collections.unmodifiableList(list);
        SUPPORTED_IS_NOT_EMPTY_TYPES = Collections.unmodifiableList(list);

        list = new ArrayList<>(Arrays.asList(RealmFieldType.values()));
        list.removeAll(SUPPORTED_IS_EMPTY_TYPES);
        list.remove(RealmFieldType.TYPED_LINK);
        list.remove(RealmFieldType.LINK_SET);

        NOT_SUPPORTED_IS_EMPTY_TYPES = Collections.unmodifiableList(list);
        NOT_SUPPORTED_IS_NOT_EMPTY_TYPES = Collections.unmodifiableList(list);
    }

    protected Realm realm;

    @Before
    public void setUp() throws Exception {
        Realm.init(ApplicationProvider.getApplicationContext());
        configFactory.create(); // Creates temporary folder (unsure why this is needed when Running RealmQueryTests independently.
        RealmConfiguration realmConfig = configFactory.createSchemaConfiguration(true, io.realm.entities.conflict.AllJavaTypes.class);
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    @SuppressWarnings("unchecked")
    protected final void createIsEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypesUnsupportedTypes emptyValues = new AllJavaTypesUnsupportedTypes();
        emptyValues.setFieldId(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<>());
        emptyValues.setColumnRealmDictionary(new RealmDictionary<>());

        AllJavaTypesUnsupportedTypes emptyValuesManaged = realm.copyToRealm(emptyValues);
        AllJavaTypesUnsupportedTypes nonEmpty = new AllJavaTypesUnsupportedTypes();
        nonEmpty.setFieldId(2);
        nonEmpty.setFieldString("Foo");
        nonEmpty.setFieldBinary(new byte[] {1, 2, 3});
        nonEmpty.setFieldObject(nonEmpty);
        nonEmpty.setFieldList(new RealmList<>(emptyValuesManaged));

        AllJavaTypesUnsupportedTypes nonEmptyManaged = realm.copyToRealmOrUpdate(nonEmpty);
        AllJavaTypesUnsupportedTypes emptyValues2 = new AllJavaTypesUnsupportedTypes();
        emptyValues2.setFieldId(3);
        emptyValues2.setFieldString("");
        emptyValues2.setFieldBinary(new byte[0]);
        emptyValues2.setFieldObject(null);
        emptyValues2.setFieldList(new RealmList<>(nonEmptyManaged));
        emptyValues2.setColumnRealmDictionary(new RealmDictionary<>());
        realm.copyToRealm(emptyValues2);

        realm.commitTransaction();
    }

    protected final void createLinkedDataSet(Realm realm) {
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

    @SuppressWarnings("unchecked")
    protected final void createIsNotEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypesUnsupportedTypes emptyValues = new AllJavaTypesUnsupportedTypes();
        emptyValues.setFieldId(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<>());
        realm.copyToRealm(emptyValues);

        AllJavaTypesUnsupportedTypes notEmpty = new AllJavaTypesUnsupportedTypes();
        notEmpty.setFieldId(2);
        notEmpty.setFieldString("Foo");
        notEmpty.setFieldBinary(new byte[] {1, 2, 3});
        notEmpty.setFieldObject(notEmpty);

        notEmpty.setFieldList(new RealmList<>(emptyValues));
        notEmpty.setFieldIntegerList(new RealmList<>(1));
        notEmpty.setFieldBooleanList(new RealmList<>(true));
        notEmpty.setFieldStringList(new RealmList<>("hello"));
        notEmpty.setFieldBinaryList(new RealmList<>(new byte[1]));
        notEmpty.setFieldDateList(new RealmList<>(new Date()));
        notEmpty.setFieldFloatList(new RealmList<>(1F));
        notEmpty.setFieldDoubleList(new RealmList<>(1.0));
        notEmpty.setFieldDecimal128List(new RealmList<>(new Decimal128(1L)));
        notEmpty.setFieldObjectIdList(new RealmList<>(new ObjectId()));
        notEmpty.setFieldUUIDList(new RealmList<>(UUID.randomUUID()));
        notEmpty.setFieldRealmAnyList(new RealmList<>(RealmAny.valueOf(1)));

        notEmpty.setColumnRealmDictionary((RealmDictionary<AllJavaTypesUnsupportedTypes>) getDictionary(RealmFieldType.STRING_TO_LINK_MAP, notEmpty));
        notEmpty.setColumnIntegerDictionary((RealmDictionary<Integer>) getDictionary(RealmFieldType.STRING_TO_INTEGER_MAP));
        notEmpty.setColumnBooleanDictionary((RealmDictionary<Boolean>) getDictionary(RealmFieldType.STRING_TO_BOOLEAN_MAP));
        notEmpty.setColumnStringDictionary((RealmDictionary<String>) getDictionary(RealmFieldType.STRING_TO_STRING_MAP));
        notEmpty.setColumnBinaryDictionary((RealmDictionary<byte[]>) getDictionary(RealmFieldType.STRING_TO_BINARY_MAP));
        notEmpty.setColumnDateDictionary((RealmDictionary<Date>) getDictionary(RealmFieldType.STRING_TO_DATE_MAP));
        notEmpty.setColumnFloatDictionary((RealmDictionary<Float>) getDictionary(RealmFieldType.STRING_TO_FLOAT_MAP));
        notEmpty.setColumnDoubleDictionary((RealmDictionary<Double>) getDictionary(RealmFieldType.STRING_TO_DOUBLE_MAP));
        notEmpty.setColumnDecimal128Dictionary((RealmDictionary<Decimal128>) getDictionary(RealmFieldType.STRING_TO_DECIMAL128_MAP));
        notEmpty.setColumnObjectIdDictionary((RealmDictionary<ObjectId>) getDictionary(RealmFieldType.STRING_TO_OBJECT_ID_MAP));
        notEmpty.setColumnUUIDDictionary((RealmDictionary<UUID>) getDictionary(RealmFieldType.STRING_TO_UUID_MAP));
        notEmpty.setColumnRealmAnyDictionary((RealmDictionary<RealmAny>) getDictionary(RealmFieldType.STRING_TO_MIXED_MAP));

        notEmpty.setColumnRealmSet((RealmSet<AllJavaTypesUnsupportedTypes>) getSet(RealmFieldType.LINK_SET, notEmpty));
        notEmpty.setColumnIntegerSet((RealmSet<Integer>) getSet(RealmFieldType.INTEGER_SET));
        notEmpty.setColumnBooleanSet((RealmSet<Boolean>) getSet(RealmFieldType.BOOLEAN_SET));
        notEmpty.setColumnStringSet((RealmSet<String>) getSet(RealmFieldType.STRING_SET));
        notEmpty.setColumnBinarySet((RealmSet<byte[]>) getSet(RealmFieldType.BINARY_SET));
        notEmpty.setColumnDateSet((RealmSet<Date>) getSet(RealmFieldType.DATE_SET));
        notEmpty.setColumnFloatSet((RealmSet<Float>) getSet(RealmFieldType.FLOAT_SET));
        notEmpty.setColumnDoubleSet((RealmSet<Double>) getSet(RealmFieldType.DOUBLE_SET));
        notEmpty.setColumnDecimal128Set((RealmSet<Decimal128>) getSet(RealmFieldType.DECIMAL128_SET));
        notEmpty.setColumnObjectIdSet((RealmSet<ObjectId>) getSet(RealmFieldType.OBJECT_ID_SET));
        notEmpty.setColumnUUIDSet((RealmSet<UUID>) getSet(RealmFieldType.UUID_SET));
        notEmpty.setColumnRealmAnySet((RealmSet<RealmAny>) getSet(RealmFieldType.MIXED_SET));

        realm.copyToRealmOrUpdate(notEmpty);

        realm.commitTransaction();
    }

    private RealmSet<?> getSet(RealmFieldType type) {
        return getSet(type, null);
    }

    private RealmSet<?> getSet(RealmFieldType type, @Nullable AllJavaTypesUnsupportedTypes obj) {
        switch (type) {
            case INTEGER_SET: {
                RealmSet<Integer> set = new RealmSet<>();
                set.add(1);
                return set;
            }
            case BOOLEAN_SET: {
                RealmSet<Boolean> set = new RealmSet<>();
                set.add(true);
                return set;
            }
            case STRING_SET: {
                RealmSet<String> set = new RealmSet<>();
                set.add("VALUE");
                return set;
            }
            case BINARY_SET: {
                RealmSet<byte[]> set = new RealmSet<>();
                set.add(new byte[1]);
                return set;
            }
            case DATE_SET: {
                RealmSet<Date> set = new RealmSet<>();
                set.add(new Date());
                return set;
            }
            case FLOAT_SET: {
                RealmSet<Float> set = new RealmSet<>();
                set.add(1.0F);
                return set;
            }
            case DOUBLE_SET: {
                RealmSet<Double> set = new RealmSet<>();
                set.add(1D);
                return set;
            }
            case DECIMAL128_SET: {
                RealmSet<Decimal128> set = new RealmSet<>();
                set.add(new Decimal128(1L));
                return set;
            }
            case OBJECT_ID_SET: {
                RealmSet<ObjectId> set = new RealmSet<>();
                set.add(new ObjectId());
                return set;
            }
            case UUID_SET: {
                RealmSet<UUID> set = new RealmSet<>();
                set.add(UUID.randomUUID());
                return set;
            }
            case LINK_SET: {
                RealmSet<AllJavaTypesUnsupportedTypes> set = new RealmSet<>();
                set.add(obj);
                return set;
            }
            case MIXED_SET: {
                RealmSet<RealmAny> set = new RealmSet<>();
                set.add(RealmAny.valueOf(1));
                return set;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private RealmDictionary<?> getDictionary(RealmFieldType type) {
        return getDictionary(type, null);
    }

    private RealmDictionary<?> getDictionary(RealmFieldType type, @Nullable AllJavaTypesUnsupportedTypes obj) {
        switch (type) {
            case STRING_TO_INTEGER_MAP: {
                RealmDictionary<Integer> dict = new RealmDictionary<>();
                dict.put("KEY", 1);
                return dict;
            }
            case STRING_TO_BOOLEAN_MAP: {
                RealmDictionary<Boolean> dict = new RealmDictionary<>();
                dict.put("KEY", true);
                return dict;
            }
            case STRING_TO_STRING_MAP: {
                RealmDictionary<String> dict = new RealmDictionary<>();
                dict.put("KEY", "VALUE");
                return dict;
            }
            case STRING_TO_BINARY_MAP: {
                RealmDictionary<byte[]> dict = new RealmDictionary<>();
                dict.put("KEY", new byte[1]);
                return dict;
            }
            case STRING_TO_DATE_MAP: {
                RealmDictionary<Date> dict = new RealmDictionary<>();
                dict.put("KEY", new Date());
                return dict;
            }
            case STRING_TO_FLOAT_MAP: {
                RealmDictionary<Float> dict = new RealmDictionary<>();
                dict.put("KEY", 1.0F);
                return dict;
            }
            case STRING_TO_DOUBLE_MAP: {
                RealmDictionary<Double> dict = new RealmDictionary<>();
                dict.put("KEY", 1D);
                return dict;
            }
            case STRING_TO_DECIMAL128_MAP: {
                RealmDictionary<Decimal128> dict = new RealmDictionary<>();
                dict.put("KEY", new Decimal128(1L));
                return dict;
            }
            case STRING_TO_OBJECT_ID_MAP: {
                RealmDictionary<ObjectId> dict = new RealmDictionary<>();
                dict.put("KEY", new ObjectId());
                return dict;
            }
            case STRING_TO_UUID_MAP: {
                RealmDictionary<UUID> dict = new RealmDictionary<>();
                dict.put("KEY", UUID.randomUUID());
                return dict;
            }
            case STRING_TO_MIXED_MAP: {
                RealmDictionary<RealmAny> dict = new RealmDictionary<>();
                dict.put("KEY", RealmAny.valueOf(1));
                return dict;
            }
            case STRING_TO_LINK_MAP: {
                RealmDictionary<AllJavaTypesUnsupportedTypes> dict = new RealmDictionary<>();
                dict.put("KEY", obj);
                return dict;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
