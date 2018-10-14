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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface used to mark a class that can be persisted by Realm.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmClass {

    /**
     * Manually set the internal name used by Realm for this class. If this class is part of
     * any modules, this will also override any name policy set using
     * {@link RealmModule#classNamingPolicy()}.
     *
     * @see io.realm.annotations.RealmNamingPolicy for more information about what setting the name means.
     * @see #name()
     */
    String value() default "";

    /**
     * Manually set the internal name used by Realm for this class. If this class is part of
     * any modules, this will also override any name policy set using
     * {@link RealmModule#classNamingPolicy()}.
     *
     * @see io.realm.annotations.RealmNamingPolicy for more information about what setting the name means.
     */
    String name() default "";

    /**
     * The naming policy applied to all fields in this class. The default policy is {@link RealmNamingPolicy#NO_POLICY}.
     * <p>
     * It is possible to override the naming policy for each field by using the {@link RealmField} annotation.
     *
     * @see io.realm.annotations.RealmNamingPolicy for more information about what setting this policy means.
     */
    RealmNamingPolicy fieldNamingPolicy() default RealmNamingPolicy.NO_POLICY;
}
