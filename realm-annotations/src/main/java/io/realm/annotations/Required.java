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
 * This annotation will mark the field or the element of a primitive {@link io.realm.RealmList} as not nullable.
 * <p>
 * When a field of type {@code Boolean, Byte, Short, Integer, Long, Float, Double, String, byte[], Date} is annotated
 * with {@link Required}, it cannot be set to {@code null}.
 * <p>
 * Fields with primitive types are implicitly required.
 * <p>
 * When a primitive {@link io.realm.RealmList} ({@code RealmList<String>, RealmList<byte[]>, RealmList<Boolean>,
 * RealmList<Byte>, RealmList<Short>, RealmList<Integer>, RealmList<Long>, RealmList<Float>, RealmList<Double>,
 * RealmList<Date>}) is annotated with {@link Required}, it cannot contain {@code null} values.
 * <p>
 * The {@link io.realm.RealmList} field itself is required always.
 * <p>
 * Compiling will fail when fields with other types have {@link Required} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {

}