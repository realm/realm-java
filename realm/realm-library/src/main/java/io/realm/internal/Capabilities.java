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

package io.realm.internal;

import javax.annotation.Nullable;


/**
 * To describe what the Realm instance can do associated with the thread it is created on.
 * The capabilities are determined when the Realm gets created. This interface could be called from another thread which
 * is different from where the Realm is created on.
 */
public interface Capabilities {
    /**
     * Return true if this Realm can be notified by another thread.
     *
     * @return true if this Realm can be notified from an other thread.
     */
    boolean canDeliverNotification();

    /**
     * Check if a Realm is able to receive a notification. If not, an {@link IllegalStateException} should be be thrown.
     *
     * @param exceptionMessage message which is contained in the exception.
     */
    void checkCanDeliverNotification(@Nullable String exceptionMessage);

    /**
     * Multiple threads might be able to deliver notifications, but the Main thread in GUI applications often have
     * special rules that need to be enforced.
     */
    boolean isMainThread();
}
