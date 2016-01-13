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

package io.realm.examples.robolectric;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.annotation.TestTarget;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.examples.robolectric.entities.Person;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RobolectricTest {
    protected final static int TEST_DATA_SIZE = 10;

    protected List<String> columnData = new ArrayList<String>();

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_DOUBLE = "columnDouble";
    private final static String FIELD_BOOLEAN = "columnBoolean";
    private final static String FIELD_DATE = "columnDate";

    private Realm testRealm;
    private Activity context;
    private RealmConfiguration testConfig;

    private Context getContext() {
        return context;
    }

    @BeforeClass
    public static void beforeClassSetUp() {
        System.setProperty("java.library.path", "./robolectricLibs");
        ShadowLog.stream = System.out;
    }

    @Before
    public void setUp() throws Exception {
        context = Robolectric.setupActivity(MainActivity.class);
        testConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(testConfig);
        testRealm = Realm.getInstance(testConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(testRealm.isEmpty());
        Person person = new Person();
        person.setName("Brad Pitt");
        person.setAge(52);
        testRealm.beginTransaction();
        testRealm.copyToRealm(person);
        testRealm.commitTransaction();
        assertFalse(testRealm.isEmpty());
    }

    @Test
    public void testCreateAndQuery() {
        Person brad = new Person();
        brad.setName("Brad Pitt");
        brad.setAge(52);
        Person angelina = new Person();
        angelina.setName("Angelina Jolie");
        angelina.setAge(40);
        testRealm.beginTransaction();
        testRealm.copyToRealm(brad);
        testRealm.copyToRealm(angelina);
        testRealm.commitTransaction();
        Person person = testRealm.where(Person.class).equalTo("name", "Angelina Jolie").findFirst();
        assertEquals("Angelina Jolie", person.getName());
        assertEquals(40, person.getAge());
    }
}
