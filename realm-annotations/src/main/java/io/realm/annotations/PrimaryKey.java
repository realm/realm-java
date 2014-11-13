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

package io.realm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @PrimaryKey annotation will mark a field as a primary key inside Realm. The property
 * should uniquely identify the object. Trying to insert an object with an existing primary key
 * will result in a {@link io.realm.exceptions.RealmException}. This check is made across all
 * objects of the same type in Realm.
 *
 * Primary keys also count as having the {@link io.realm.annotations.Index} annotation.
 *
 * Only one field pr. model clas can have this field, and it is only allowed on the the following
 * types: String, Short, Integer, Long, short, int, long
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

}
