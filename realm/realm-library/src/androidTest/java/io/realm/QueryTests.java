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
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.UiThreadTestRule;
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
                RealmFieldType.STRING_TO_LINK_MAP
        ));

        SUPPORTED_IS_EMPTY_TYPES = Collections.unmodifiableList(list);
        SUPPORTED_IS_NOT_EMPTY_TYPES = Collections.unmodifiableList(list);

        list = new ArrayList<>(Arrays.asList(RealmFieldType.values()));
        list.removeAll(SUPPORTED_IS_EMPTY_TYPES);
        list.remove(RealmFieldType.TYPED_LINK);

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
        nonEmpty.setColumnRealmDictionary(new RealmDictionary<>());
        nonEmpty.getColumnRealmDictionary().put("key", emptyValuesManaged);

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

    protected final void createIsNotEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypesUnsupportedTypes emptyValues = new AllJavaTypesUnsupportedTypes();
        emptyValues.setFieldId(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<AllJavaTypesUnsupportedTypes>());
        realm.copyToRealm(emptyValues);

        AllJavaTypesUnsupportedTypes notEmpty = new AllJavaTypesUnsupportedTypes();
        notEmpty.setFieldId(2);
        notEmpty.setFieldString("Foo");
        notEmpty.setFieldBinary(new byte[] {1, 2, 3});
        notEmpty.setFieldObject(notEmpty);
        notEmpty.setFieldList(new RealmList<AllJavaTypesUnsupportedTypes>(emptyValues));
        realm.copyToRealmOrUpdate(notEmpty);

        realm.commitTransaction();
    }
}
