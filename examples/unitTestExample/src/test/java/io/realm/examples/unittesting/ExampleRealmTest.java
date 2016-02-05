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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.realm.Realm;
import io.realm.examples.unittesting.model.Dog;
import io.realm.examples.unittesting.repository.DogRepository;
import io.realm.examples.unittesting.repository.DogRepositoryImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest({Realm.class})
public class ExampleRealmTest {
    // Robolectric, Using Power Mock https://github.com/robolectric/robolectric/wiki/Using-PowerMock

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    Realm mockRealm;

    @Before
    public void setup() {
        mockStatic(Realm.class);

        Realm mockRealm = PowerMockito.mock(Realm.class);

        when(Realm.getDefaultInstance()).thenReturn(mockRealm);

        this.mockRealm = mockRealm;
    }

    @Test
    public void shouldBeAbleToGetDefaultInstance() {
        assertThat(Realm.getDefaultInstance(), is(mockRealm));
    }

    @Test
    public void shouldBeAbleToMockRealmMethods() {
        when(mockRealm.isAutoRefresh()).thenReturn(true);
        assertThat(mockRealm.isAutoRefresh(), is(true));

        when(mockRealm.isAutoRefresh()).thenReturn(false);
        assertThat(mockRealm.isAutoRefresh(), is(false));
    }

    @Test
    public void shouldBeAbleToCreateARealmObject() {
        Dog dog = new Dog();
        when(mockRealm.createObject(Dog.class)).thenReturn(dog);

        Dog output = mockRealm.createObject(Dog.class);

        assertThat(output, is(dog));
    }

    /**
     * This test verifies the behavior in the {@link DogRepositoryImpl} class.
     */
    @Test
    public void shouldVerifyTransactionWasCreated() {

        Dog dog = mock(Dog.class);
        when(mockRealm.createObject(Dog.class)).thenReturn(dog);

        DogRepository dogRepo = new DogRepositoryImpl();
        dogRepo.createDog("Spot");

        // Verify that the begin transaction was called only once
        verify(mockRealm, times(1)).beginTransaction();

        // Verify that Realm#createObject was called only once
        verify(mockRealm, times(1)).createObject(Dog.class); // Verify that a Dog was in fact created.

        // Verify that Dog#setName() is called only once
        verify(dog, times(1)).setName(Mockito.anyString()); // Any string will do

        // Verify that the transaction was committed only once
        verify(mockRealm, times(1)).commitTransaction();

        // Verify that the Realm was closed only once.
        verify(mockRealm, times(1)).close();
    }
}
