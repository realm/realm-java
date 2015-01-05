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

    private Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        for (int i = 0; i < 10; i++) {
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
        Dog dog5 = owner.getDogs().get(5);
        owner.getDogs().move(5, 6); // This doesn't do anything as index 5 is now empty so the index's above gets shifted to the left.

        assertEquals(10, owner.getDogs().size());
        assertEquals(5, owner.getDogs().indexOf(dog5));
    }

    public void testMoveOutOfBoundsLowerThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        try {
            owner.getDogs().move(1, -1);
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        fail("Indexes < 0 should throw an exception");
    }

    public void testMoveOutOfBoundsHigherThrows() {
        Owner owner = testRealm.where(Owner.class).findFirst();
        try {
            owner.getDogs().move(9, 10);
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        fail("Indexes >= size() should throw an exception");
    }
}
