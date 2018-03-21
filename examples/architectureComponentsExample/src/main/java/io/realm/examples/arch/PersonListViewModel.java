/*
 * Copyright 2018 Realm Inc.
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
package io.realm.examples.arch;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.realm.Realm;
import io.realm.examples.arch.livemodel.LiveRealmResults;
import io.realm.examples.arch.model.Person;

public class PersonListViewModel extends ViewModel {
    private final Realm realm;
    private final LiveData<List<Person>> persons;

    public PersonListViewModel() {
        realm = Realm.getDefaultInstance(); // Realm is bound to the lifecycle of the ViewModel, and stays alive as long as it is needed.
        persons = new LiveRealmResults<>(realm.where(Person.class).sort("age").findAllAsync());
    }

    public LiveData<List<Person>> getPersons() {
        return persons;
    }

    @Override
    protected void onCleared() {
        realm.close(); // Realm is bound to the lifecycle of the ViewModel, and is destroyed when no longer needed.
        super.onCleared();
    }
}
