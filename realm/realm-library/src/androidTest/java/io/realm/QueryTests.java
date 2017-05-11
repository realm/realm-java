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

import io.realm.entities.AllJavaTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;


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

    protected static final List<RealmFieldType> SUPPORTED_IS_EMPTY_TYPES;
    protected static final List<RealmFieldType> NOT_SUPPORTED_IS_EMPTY_TYPES;
    protected static final List<RealmFieldType> SUPPORTED_IS_NOT_EMPTY_TYPES;
    protected static final List<RealmFieldType> NOT_SUPPORTED_IS_NOT_EMPTY_TYPES;

    static {
        ArrayList<RealmFieldType> list = new ArrayList<>(Arrays.asList(
                RealmFieldType.STRING,
                RealmFieldType.BINARY,
                RealmFieldType.LIST));
                // TODO: LINKING_OBJECTS should be supported
        SUPPORTED_IS_EMPTY_TYPES = Collections.unmodifiableList(list);
        SUPPORTED_IS_NOT_EMPTY_TYPES = Collections.unmodifiableList(list);

        list = new ArrayList<>(Arrays.asList(RealmFieldType.values()));
        list.removeAll(SUPPORTED_IS_EMPTY_TYPES);
        list.remove(RealmFieldType.UNSUPPORTED_MIXED);
        list.remove(RealmFieldType.UNSUPPORTED_TABLE);
        list.remove(RealmFieldType.UNSUPPORTED_DATE);
        list.remove(RealmFieldType.LINKING_OBJECTS);
        NOT_SUPPORTED_IS_EMPTY_TYPES = Collections.unmodifiableList(list);
        NOT_SUPPORTED_IS_NOT_EMPTY_TYPES = Collections.unmodifiableList(list);
    }

    protected Realm realm;

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
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

        AllJavaTypes emptyValues = new AllJavaTypes();
        emptyValues.setFieldId(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<AllJavaTypes>());
        realm.copyToRealm(emptyValues);

        AllJavaTypes nonEmpty = new AllJavaTypes();
        nonEmpty.setFieldId(2);
        nonEmpty.setFieldString("Foo");
        nonEmpty.setFieldBinary(new byte[] {1, 2, 3});
        nonEmpty.setFieldObject(nonEmpty);
        nonEmpty.setFieldList(new RealmList<AllJavaTypes>(emptyValues));
        realm.copyToRealmOrUpdate(nonEmpty);

        realm.commitTransaction();
    }

    protected final void createIsNotEmptyDataSet(Realm realm) {
        realm.beginTransaction();

        AllJavaTypes emptyValues = new AllJavaTypes();
        emptyValues.setFieldId(1);
        emptyValues.setFieldString("");
        emptyValues.setFieldBinary(new byte[0]);
        emptyValues.setFieldObject(emptyValues);
        emptyValues.setFieldList(new RealmList<AllJavaTypes>());
        realm.copyToRealm(emptyValues);

        AllJavaTypes notEmpty = new AllJavaTypes();
        notEmpty.setFieldId(2);
        notEmpty.setFieldString("Foo");
        notEmpty.setFieldBinary(new byte[] {1, 2, 3});
        notEmpty.setFieldObject(notEmpty);
        notEmpty.setFieldList(new RealmList<AllJavaTypes>(emptyValues));
        realm.copyToRealmOrUpdate(notEmpty);

        realm.commitTransaction();
    }
}
