/*
 * Copyright 2016 Realm Inc.
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
import io.realm.annotations.Ignore;

public class CustomMethods extends RealmObject {
    public static final String CUSTOM_TO_STRING = "custom toString";
    public static final int HASHCODE = 1;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Ignore
    public boolean reverseEquals;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomMethods)) {
            return reverseEquals;
        }
        CustomMethods other = (CustomMethods) o;
        if (isValid() == other.isValid() && other.name.equals(name)) {
            return !reverseEquals;
        } else {
            return reverseEquals;
        }
    }

    @Override
    public String toString() {
        return CUSTOM_TO_STRING;
    }

    @Override
    public int hashCode() {
        return HASHCODE;
    }
}
