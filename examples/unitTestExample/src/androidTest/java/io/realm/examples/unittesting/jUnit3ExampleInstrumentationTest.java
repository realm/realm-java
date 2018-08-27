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

import android.test.InstrumentationTestCase;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.examples.unittesting.repository.DogRepository;
import io.realm.examples.unittesting.repository.DogRepositoryImpl;

public class jUnit3ExampleInstrumentationTest extends InstrumentationTestCase {

    private DogRepository dogRepository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Realm.init(getInstrumentation().getContext());
        dogRepository = new DogRepositoryImpl(new RealmConfiguration.Builder()
                .name("integrationTest")
                .inMemory()
                .build()
        );
    }

    public void testShouldBeAbleToLaunchActivityAndSeeRealmResults() {
        assertNull(dogRepository.findDogNamged("Laika"));
        dogRepository.createDog("Laika");
        assertEquals("laika", dogRepository.findDogNamged("Laika").getName());
    }
}
