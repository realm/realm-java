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

import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.Owner;

public class RealmLinkTests extends AndroidTestCase {

    protected Realm testRealm;

    protected void setUp() {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        testRealm.clear(Owner.class);

        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Pluto");
        dog1.setAge(5);

        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Fido");
        dog2.setAge(10);

        Cat cat = testRealm.createObject(Cat.class);
        cat.setName("Blackie");
        cat.setAge(12);

        Owner owner = testRealm.createObject(Owner.class);
        owner.setName("Tim");
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        owner.setCat(cat);

        testRealm.commitTransaction();
    }

    public void testObjects() {
        RealmResults<Owner> owners = testRealm.allObjects(Owner.class);
        assertEquals(1, owners.size());
        assertEquals(2, owners.first().getDogs().size());
        assertEquals("Pluto", owners.first().getDogs().first().getName());
        assertEquals("Fido", owners.first().getDogs().last().getName());
        assertEquals("Blackie", owners.first().getCat().getName());
        assertEquals(12, owners.first().getCat().getAge());
    }

    public void testQuerySingleRelationInteger() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("cat.age", 12).findAll();
        assertEquals(1, owners.size());
        assertEquals(12, owners.first().getCat().getAge());

        RealmResults<Owner> none = testRealm.where(Owner.class).equalTo("cat.age", 13).findAll();
        assertEquals(0, none.size());
    }

    public void testQuerySingleRelationString() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("cat.name", "Blackie").findAll();
        assertEquals(1, owners.size());

        RealmResults<Owner> none = testRealm.where(Owner.class).equalTo("cat.name", "Max").findAll();
        assertEquals(0, none.size());
    }

    public void testQueryMultipleRelationsInteger() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("dogs.age", 10).findAll();
        assertEquals(1, owners.size());

        RealmResults<Owner> none = testRealm.where(Owner.class).equalTo("dogs.age", 7).findAll();
        assertEquals(0, none.size());
    }
    
    public void testQueryMultipleRelationsString() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("dogs.name", "Pluto").findAll();
        assertEquals(1, owners.size());

        RealmResults<Owner> none = testRealm.where(Owner.class).equalTo("dogs.name", "King").findAll();
        assertEquals(0, none.size());
    }
}
