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

package io.realm.internal.migration;

import io.realm.examples.junkyard.DirectMigrationIterator;
import io.realm.examples.junkyard.MigrationIterator;

public abstract class Migration {

    public abstract void addClass(RealmObjectSpec manual);

    public abstract void replaceSpec(RealmObjectSpec autoSpec);

    public abstract void removeClass(RealmObjectSpec classSpec);

    public abstract void removeClass(String className);

    public abstract RealmObjectSpec getClass(String aNew);

    public abstract void renameClass(String old, String aNew);

    public abstract Migration copy();

    public abstract void forEach(String aNew, MigrationIterator migrationIterator);
    public abstract void forEach(String aNew, DirectMigrationIterator migrationIterator);

}
