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

package io.realm.examples.unittesting;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.examples.unittesting.model.Person;
import io.realm.internal.RealmCore;
import io.realm.log.RealmLog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class, RealmLog.class})
public class ExampleActivityTest {
    // Robolectric, Using Power Mock https://github.com/robolectric/robolectric/wiki/Using-PowerMock

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private Realm mockRealm;
    private RealmResults<Person> people;

    @Before
    public void setup() throws Exception {

        // Setup Realm to be mocked. The order of these matters
        mockStatic(RealmCore.class);
        mockStatic(RealmLog.class);
        mockStatic(Realm.class);
        mockStatic(RealmConfiguration.class);
        Realm.init(RuntimeEnvironment.application);

        // Create the mock
        final Realm mockRealm = mock(Realm.class);
        final RealmConfiguration mockRealmConfig = mock(RealmConfiguration.class);

        // TODO: Better solution would be just mock the RealmConfiguration.Builder class. But it seems there is some
        // problems for powermock to mock it (static inner class). We just mock the RealmCore.loadLibrary(Context) which
        // will be called by RealmConfiguration.Builder's constructor.
        doNothing().when(RealmCore.class);
        RealmCore.loadLibrary(any(Context.class));


        // TODO: Mock the RealmConfiguration's constructor. If the RealmConfiguration.Builder.build can be mocked, this
        // is not necessary anymore.
        whenNew(RealmConfiguration.class).withAnyArguments().thenReturn(mockRealmConfig);

        // Anytime getInstance is called with any configuration, then return the mockRealm
        when(Realm.getDefaultInstance()).thenReturn(mockRealm);

        // Anytime we ask Realm to create a Person, return a new instance.
        when(mockRealm.createObject(Person.class)).thenReturn(new Person());

        // Set up some naive stubs
        Person p1 = new Person();
        p1.setAge(14);
        p1.setName("John Young");

        Person p2 = new Person();
        p2.setAge(89);
        p2.setName("John Senior");

        Person p3 = new Person();
        p3.setAge(27);
        p3.setName("Jane");

        Person p4 = new Person();
        p4.setAge(42);
        p4.setName("Robert");

        List<Person> personList = Arrays.asList(p1, p2, p3, p4);

        // Create a mock RealmQuery
        RealmQuery<Person> personQuery = mockRealmQuery();

        // When the RealmQuery performs findFirst, return the first record in the list.
        when(personQuery.findFirst()).thenReturn(personList.get(0));

        // When the where clause is called on the Realm, return the mock query.
        when(mockRealm.where(Person.class)).thenReturn(personQuery);

        // When the RealmQuery is filtered on any string and any integer, return the person query
        when(personQuery.equalTo(anyString(), anyInt())).thenReturn(personQuery);

        // RealmResults is final, must mock static and also place this in the PrepareForTest annotation array.
        mockStatic(RealmResults.class);

        // Create a mock RealmResults
        RealmResults<Person> people = mockRealmResults();

        // When we ask Realm for all of the Person instances, return the mock RealmResults
        when(mockRealm.where(Person.class).findAll()).thenReturn(people);

        // When a between query is performed with any string as the field and any int as the
        // value, then return the personQuery itself
        when(personQuery.between(anyString(), anyInt(), anyInt())).thenReturn(personQuery);

        // When a beginsWith clause is performed with any string field and any string value
        // return the same person query
        when(personQuery.beginsWith(anyString(), anyString())).thenReturn(personQuery);

        // When we ask the RealmQuery for all of the Person objects, return the mock RealmResults
        when(personQuery.findAll()).thenReturn(people);


        // The for(...) loop in Java needs an iterator, so we're giving it one that has items,
        // since the mock RealmResults does not provide an implementation. Therefore, anytime
        // anyone asks for the RealmResults Iterator, give them a functioning iterator from the
        // ArrayList of Persons we created above. This will allow the loop to execute.
        when(people.iterator()).thenReturn(personList.iterator());

        // Return the size of the mock list.
        when(people.size()).thenReturn(personList.size());

        this.mockRealm = mockRealm;
        this.people = people;
    }


    @Test
    public void shouldBeAbleToAccessActivityAndVerifyRealmInteractions() {
        doCallRealMethod().when(mockRealm).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Create activity
        ExampleActivity activity = Robolectric.buildActivity(ExampleActivity.class).create().start().resume().visible().get();

        assertThat(activity.getTitle().toString(), is("Unit Test Example"));

        // Verify that two Realm.getInstance() calls took place.
        verifyStatic(times(2));
        Realm.getDefaultInstance();

        // verify that we have four begin and commit transaction calls
        // Do not verify partial mock invocation count: https://github.com/jayway/powermock/issues/649
        //verify(mockRealm, times(4)).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Click the clean up button
        activity.findViewById(R.id.clean_up).performClick();

        // Verify that begin and commit transaction were called (been called a total of 5 times now)
        // Do not verify partial mock invocation count: https://github.com/jayway/powermock/issues/649
        //verify(mockRealm, times(5)).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Verify that we queried for Person instances five times in this run (2 in basicCrud(),
        // 2 in complexQuery() and 1 in the button click)
        verify(mockRealm, times(5)).where(Person.class);

        // Verify that the delete method was called. Delete is also called in the start of the
        // activity to ensure we start with a clean db.
        verify(mockRealm, times(2)).delete(Person.class);

        // Call the destroy method so we can verify that the .close() method was called (below)
        activity.onDestroy();

        // Verify that the realm got closed 2 separate times. Once in the AsyncTask, once
        // in onDestroy
        verify(mockRealm, times(2)).close();
    }

    /**
     * Have to verify the transaction execution in a different test because
     * of a problem with Powermock: https://github.com/jayway/powermock/issues/649
     */
    @Test
    public void shouldBeAbleToVerifyTransactionCalls() {

        // Create activity
        ExampleActivity activity = Robolectric.buildActivity(ExampleActivity.class).create().start().resume().visible().get();

        assertThat(activity.getTitle().toString(), is("Unit Test Example"));

        // Verify that two Realm.getInstance() calls took place.
        verifyStatic(times(2));
        Realm.getDefaultInstance();

        // verify that we have four begin and commit transaction calls
        // Do not verify partial mock invocation count: https://github.com/jayway/powermock/issues/649
        verify(mockRealm, times(4)).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Click the clean up button
        activity.findViewById(R.id.clean_up).performClick();

        // Verify that begin and commit transaction were called (been called a total of 5 times now)
        // Do not verify partial mock invocation count: https://github.com/jayway/powermock/issues/649
        verify(mockRealm, times(5)).executeTransaction(Mockito.any(Realm.Transaction.class));

        // Call the destroy method so we can verify that the .close() method was called (below)
        activity.onDestroy();

        // Verify that the realm got closed 2 separate times. Once in the AsyncTask, once
        // in onDestroy
        verify(mockRealm, times(2)).close();
    }


    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmQuery<T> mockRealmQuery() {
        return mock(RealmQuery.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends RealmObject> RealmResults<T> mockRealmResults() {
        return mock(RealmResults.class);
    }
}
