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
package io.realm.internal;

/**
 * This internal interface represents a java object that corresponds to data
 * that may be managed in the Realm core. It specifies the operations common to all such objects.
 */
public interface ManagableObject {

    /**
     * Checks to see if this object is managed by Realm..
     *
     * @return {@code true} if this is a managed Realm object, {@code false} otherwise.
     */
    boolean isManaged();

    /**
     * Checks to see if the managed object is still valid to use.
     * That is if it that it hasn't been deleted nor has the {@link io.realm.Realm} been closed.
     * It will always return {@code true} for unmanaged objects.
     *
     * @return {@code true} if this object is unmanaged or is still valid for use, {@code false} otherwise.
     */
    boolean isValid();
}
