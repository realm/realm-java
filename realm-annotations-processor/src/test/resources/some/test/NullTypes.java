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

package some.test;

import java.lang.String;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class NullTypes extends RealmObject {
    @Required
    private String fieldStringNotNull;
    private String fieldStringNull;

    @Required
    private Boolean fieldBooleanNotNull;
    private Boolean fieldBooleanNull;

    public String getFieldStringNotNull() {
        return fieldStringNotNull;
    }

    public void setFieldStringNotNull(String fieldStringNotNull) {
        this.fieldStringNotNull = fieldStringNotNull;
    }

    public String getFieldStringNull() {
        return fieldStringNull;
    }

    public void setFieldStringNull(String fieldStringNull) {
        this.fieldStringNull = fieldStringNull;
    }

    public Boolean getFieldBooleanNotNull() {
        return fieldBooleanNotNull;
    }

    public void setFieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        this.fieldBooleanNotNull = fieldBooleanNotNull;
    }

    public Boolean getFieldBooleanNull() {
        return fieldBooleanNull;
    }

    public void setFieldBooleanNull(Boolean fieldBooleanNull) {
        this.fieldBooleanNull = fieldBooleanNull;
    }
}