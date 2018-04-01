/*
 * Copyright 2018 Realm Inc.
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

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * This class check that inheritence (non-polymorphic works as intended
 */
public class InheritenceTests {

    @Test
    public void getSetFields_unmanagedObject() {
       fail();
    }

    @Test
    public void getSetFields_managedObject() {
        fail();
    }

    @Test
    public void subclassIncludeBaseClassFields() {
        fail();
    }

    @Test
    public void subClassOverrideBaseClassFieldsAndAnnotations() {
        fail();
    }

    @Test
    public void abstractClassesNotInSchema() {
        fail();
    }

    @Test
    public void where_abstractClassQueriesThrows() {
        fail();
    }

    @Test
    public void createObject_abstractClassesThrows() {
        fail();
    }

    @Test
    public void insert_abstractClassesThrows() {
        fail();
    }

    @Test
    public void copyToRealm_abstractClassesThrows() {
        fail();
    }

}
