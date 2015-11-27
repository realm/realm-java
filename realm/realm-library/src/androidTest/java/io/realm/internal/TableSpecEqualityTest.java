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

public class TableSpecEqualityTest extends TestCase {

    public void testShouldMatchIdenticalSimpleSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmFieldType.STRING, "foo");
        spec1.addColumn(RealmFieldType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmFieldType.STRING, "foo");
        spec2.addColumn(RealmFieldType.BOOLEAN, "bar");

        assertTrue(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnNames() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmFieldType.STRING, "foo");
        spec1.addColumn(RealmFieldType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmFieldType.STRING, "foo");
        spec2.addColumn(RealmFieldType.BOOLEAN, "bar2");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnTypes() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmFieldType.STRING, "foo");
        spec1.addColumn(RealmFieldType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmFieldType.STRING, "foo");
        spec2.addColumn(RealmFieldType.BINARY, "bar");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldMatchDeepRecursiveIdenticalSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmFieldType.STRING, "foo");
        spec1.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(RealmFieldType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(RealmFieldType.UNSUPPORTED_TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmFieldType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmFieldType.STRING, "foo");
        spec2.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(RealmFieldType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(RealmFieldType.UNSUPPORTED_TABLE, "sub");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmFieldType.BOOLEAN, "b");

        assertTrue(spec1.equals(spec2));
    }

    public void testShouldNotMatchDeepRecursiveDifferentSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmFieldType.STRING, "foo");
        spec1.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(RealmFieldType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(RealmFieldType.UNSUPPORTED_TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmFieldType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmFieldType.STRING, "foo");
        spec2.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(RealmFieldType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(RealmFieldType.UNSUPPORTED_TABLE, "sub2");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmFieldType.BOOLEAN, "b");

        assertFalse(spec1.equals(spec2));
    }

}
