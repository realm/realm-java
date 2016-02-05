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

package io.realm.internal;

import junit.framework.TestCase;

import io.realm.RealmFieldType;

public class JNIBinaryTypeTest extends TestCase {

    protected Table table;
    protected byte [] testArray = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    @Override
    public void setUp() {
        RealmCore.loadLibrary();
        //util.setDebugLevel(0); //Set to 1 to see more JNI debug messages

        table = new Table();
        table.addColumn(RealmFieldType.BINARY, "bin");
    }

    @Override
    public void tearDown() {
        //table.close();
        table = null;
    }
}
