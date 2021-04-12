/*
 * Copyright 2021 Realm Inc.
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

package io.realm;

/**
 * {@link SetChangeListener} can be registered with a {@link RealmSet} to receive a notification
 * with a {@link SetChangeSet} to describe the details of what have been changed in the map since
 * last time.
 * <p>
 * Realm instances on a thread without an {@link android.os.Looper} cannot register a
 * {@link SetChangeListener}.
 * <p>
 *
 * @param <T> the type of the values stored in the set
 * @see RealmSet#addChangeListener(SetChangeListener)
 */
public interface SetChangeListener<T> {
    void onChange(RealmSet<T> set, SetChangeSet changes);
}
