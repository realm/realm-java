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
package io.realm.entities.realmname;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;

@RealmClass("my-class-name")
public class ClassWithValueDefinedNames extends RealmObject {

    public static final String JAVA_CLASS_NAME = "ClassWithValueDefinedNames";
    public static final String REALM_CLASS_NAME = "my-class-name";

    public static final String JAVA_FIELD_NAME = "field";
    public static final String REALM_FIELD_NAME = "my-field-name";

    @RealmField("my-field-name")
    public String field;

    @RealmField("object-link")
    public ClassWithValueDefinedNames objectLink;

    // Must use the Java defined name
    // Using `@LinkingObjects("object-link")` is not supported
    @LinkingObjects("objectLink")
    public final RealmResults<ClassWithValueDefinedNames> parents = null;
}
