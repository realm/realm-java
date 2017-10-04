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

package io.realm.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for meta data about a field persisted by Realm.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmField {

    /**
     * Manually set the internal name used by Realm for this field. This will override any {@link RealmNamingPolicy}
     * otherwise set.
     *
     * @see {@link RealmNamingPolicy} for more information about what setting the name means.
     */
    String name() default "";
}
