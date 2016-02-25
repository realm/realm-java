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

package io.realm.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will mark the field as not nullable. When the field is {@link Required},
 * it cannot be set to {@code null}.
 * <p>
 * Only {@code Boolean, Byte, Short, Integer, Long, Float, Double, String, byte[], Date} can be annotated
 * with {@link Required}. Compiling will fail when fields with other types have {@link Required} annotation.
 * Fields with primitive types and the {@link io.realm.RealmList} type are required implicitly.
 * Fields with {@link io.realm.RealmObject} type are always nullable.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Required {

}