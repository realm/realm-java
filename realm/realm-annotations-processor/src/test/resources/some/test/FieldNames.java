/*
 * Copyright 2014 Realm Inc.
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

import io.realm.RealmObject;
import some.test.Simple;

/**
 * All field names should be allowed. This means that annnotation processor
 * should add a suffix to all fieldNames to avoid naming conflict with
 * internal processor variabels.
 *
 * This class list field names that has caused problems.
 */
public class FieldNames extends RealmObject implements FieldNamesRealmProxyInterface {

    private Simple name;
    private Simple cache;

    public Simple getName() {
        return realmGetter$name();
    }

    public void setName(Simple name) {
        realmSetter$name(name);
    }

    public Simple realmGetter$name() {
        return name;
    }

    public void realmSetter$name(Simple name) {
        this.name = name;
    }

    public Simple getCache() {
        return realmGetter$cache();
    }

    public void setCache(Simple cache) {
        realmSetter$cache(cache);
    }

    public Simple realmGetter$cache() {
        return cache;
    }

    public void realmSetter$cache(Simple cache) {
        this.cache = cache;
    }
}
