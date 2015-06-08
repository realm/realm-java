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

package io.realm.examples.junkyard;

import java.io.File;

public class RealmConfiguration {

    public static class Builder {

        public RealmConfiguration build() {
            return null;
        }

        public Builder realmDir(File filesDir) {
            return null;
        }

        public Builder realmName(String fileName) {
            return null;
        }

        public Builder version(int specVersion) {
            return null;
        }

        public Builder encryptionKey(byte[] keyData) {
            return null;
        }

        public Builder addClassModule(LibraryModule module) {
            return null;
        }

        public Builder useClassModule(AppModule module) {
            return null;
        }

        public Builder migrationCallback(MyDirectRealmMigration migration) {
            return null;
        }

        public Builder deletetRealmIfMigrationNeeded(boolean delete) {
            return null;
        }

        public Builder readOnly(boolean readOnly) {
            return null;
        }
    }
}
