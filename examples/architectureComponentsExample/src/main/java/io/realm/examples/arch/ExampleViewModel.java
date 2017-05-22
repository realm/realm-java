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

package io.realm.examples.arch;

import android.arch.lifecycle.ViewModel;

import io.realm.Realm;
import io.realm.examples.arch.livemodel.LiveRealmObject;
import io.realm.examples.arch.model.Person;


public class ExampleViewModel extends ViewModel {
    private final Realm realm;

    private final LiveRealmObject<Person> livePerson;

    public ExampleViewModel() {
        realm = Realm.getDefaultInstance();
        livePerson = new LiveRealmObject<>(realm.where(Person.class).findAllSorted("name").first());
    }

    public LiveRealmObject<Person> getPerson() {
        return livePerson;
    }

    @Override
    protected void onCleared() {
        realm.close();
        super.onCleared();
    }
}
