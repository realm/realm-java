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


import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.examples.unittesting.repository.DogRepository;
import io.realm.examples.unittesting.repository.DogRepositoryImpl;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class jUnit4ExampleInstrumentationTest {

    private DogRepository dogRepository;

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        dogRepository = new DogRepositoryImpl(new RealmConfiguration.Builder()
                .name("integrationTest")
                .inMemory()
                .build()
        );
    }

    @Test
    public void testCanCreateAndRetrieveDogWithName() {
        assertThat(dogRepository.findDogNamged("Laika"), is(equalTo(null)));
        dogRepository.createDog("Laika");
        assertThat(dogRepository.findDogNamged("Laika").getName(), is(equalTo("Laika")));
    }
}
