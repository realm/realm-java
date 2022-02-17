/*
 * Copyright 2020 Realm Inc.
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

package some.test;

import io.realm.RealmAny;
import io.realm.RealmDictionary;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class RealmDictionaryModelRealmAnyRequired extends RealmObject {

    @Required private RealmDictionary<RealmAny> myRequiredRealmAnyRealmDictionary;

    public RealmDictionary<RealmAny> getMyRequiredRealmAnyRealmDictionary() {
        return myRequiredRealmAnyRealmDictionary;
    }

    public void setMyRequiredRealmAnyRealmDictionary(RealmDictionary<RealmAny> myRequiredRealmAnyRealmDictionary) {
        this.myRequiredRealmAnyRealmDictionary = myRequiredRealmAnyRealmDictionary;
    }
}
