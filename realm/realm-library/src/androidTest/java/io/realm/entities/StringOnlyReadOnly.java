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

package io.realm.entities;

import io.realm.RealmObject;


/**
 * WARNING: This class is used by some `readOnly()` tests. Modifying this class will cause these to fail.
 * If this class is modified then `/assets/readonly.realm` must be re-created as well.
 */
public class StringOnlyReadOnly extends RealmObject {
    private String chars;

    public String getChars() {
        return chars;
    }

    public void setChars(String chars) {
        this.chars = chars;
    }
}
