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

package io.realm;

public interface RealmMigration {

    /**
     * Implement this method in your subclass to perform migration
     * @param realm The Realm on which to perform the migration
     * @param version The version of the Realm at the start of the migration
     * @return The version of the Realm after executing the migration
     */
    public int execute(Realm realm, int version);
}
