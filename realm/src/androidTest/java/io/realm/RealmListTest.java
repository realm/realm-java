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

import android.test.AndroidTestCase;

import io.realm.entities.Dog;
import io.realm.entities.Owner;

public class RealmListTest extends AndroidTestCase {

    public static final int TEST_OBJECTS = 10;
    private Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        owner.setName("Owner");
        for (int i = 0; i < TEST_OBJECTS; i++) {
            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Dog " + i);
            owner.getDogs().add(dog);
        }
        testRealm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    // Test move where oldPosition > newPosition
    public void testMoveDown() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog1 = owner.getDogs().get(1);
        owner.getDogs().move(1, 0);

        assertEquals(0, owner.getDogs().indexOf(dog1));
    }

    // Test move where oldPosition < newPosition
    public void testMoveUp() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog = owner.getDogs().get(0);
        int oldIndex = TEST_OBJECTS / 2;
        int newIndex = oldIndex + 1;
        owner.getDogs().move(oldIndex, newIndex); // This doesn't do anything as oldIndex is now empty so the index's above gets shifted to the left.

        assertEquals(TEST_OBJECTS, owner.getDogs().size());
        assertEquals(oldIndex, owner.getDogs().indexOf(dog));
    }

    public void testMoveOutOfBoundsLowerThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        try {
            owner.getDogs().move(0, -1);
            fail("Indexes < 0 should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    public void testMoveOutOfBoundsHigherThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        try {
            int lastIndex = TEST_OBJECTS - 1;
            int outOfBoundsIndex = TEST_OBJECTS;
            owner.getDogs().move(lastIndex, outOfBoundsIndex);
            fail("Indexes >= size() should throw an exception");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    public void testAddObject() {
        testRealm.beginTransaction();
        testRealm.clear(Owner.class);
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog = testRealm.createObject(Dog.class);
        owner.getDogs().add(dog);
        testRealm.commitTransaction();

        assertEquals(1, testRealm.where(Owner.class).findFirst().getDogs().size());
    }

    public void testAddObjectNullThrows() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        try {
            owner.getDogs().add(null);
            fail("Adding null values is not supported");
        } catch (NullPointerException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testSize() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        assertEquals(TEST_OBJECTS, owner.getDogs().size());
    }

    public void testGetObjects() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        assertNotNull(dogs);
        assertEquals("Dog 1", dogs.get(1).getName());
    }

    public void testGetFirstObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog = owner.getDogs().first();

        assertEquals("Dog 0", dog.getName());
    }

    public void testGetLastObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        Dog dog = owner.getDogs().last();

        assertEquals("Dog " + (TEST_OBJECTS - 1), dog.getName());
    }

    public void testRemoveByIndex() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();

        testRealm.beginTransaction();
        Dog removedDog = dogs.remove(5);
        testRealm.commitTransaction();

        assertNull(removedDog);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    public void testRemoveByObject() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog dog = dogs.get(0);

        testRealm.beginTransaction();
        boolean result = dogs.remove(dog);
        testRealm.commitTransaction();

        assertTrue(result);
        assertEquals(TEST_OBJECTS - 1, dogs.size());
    }

    public void testQuery() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        RealmList<Dog> dogs = owner.getDogs();
        Dog firstDog = dogs.where().equalTo("name", "Dog 0").findFirst();

        assertNotNull(firstDog);
    }
}
