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

package io.realm.tests.api;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.tests.api.entities.Dog;


public class RealmChangeListenerTest extends RealmSetupTests {
    // Notifications

    //addChangeListener(RealmChangeListener listener)
    int testCount = 0;
    public void testChangeNotify() {
        Realm realm = getTestRealm();

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

//        try {
            realm.beginWrite();
            for (int i = 0; i < 5; i++) {

                Dog dog = realm.create(Dog.class);
                dog.setName("King "+Integer.toString(testCount) );
            }

            realm.commit();
            assertTrue("Have not received the expected number of events in ChangeListener", 5 == testCount);

//        } catch (Throwable t) {
//            fail();
//        }
    }


    //void removeChangeListener(RealmChangeListener listener)
    public void testChangeNotifyRemove() {
        Realm realm = getTestRealm();
        RealmChangeListener realmChangeListener = null;
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };
        realm.addChangeListener(realmChangeListener);

        realm.removeChangeListener(realmChangeListener);
    }

    //void removeChangeListener(RealmChangeListener listener)
    public void testFailChangeNotifyRemove() {
        Realm realm = getTestRealm();
        RealmChangeListener realmChangeListener = null;
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
            }
        };

        realm.removeChangeListener(realmChangeListener);
    }


    //void removeAllChangeListeners()
    public void testRemoveAllChangeListeners() {
        Realm realm = getTestRealm();

        realm.removeAllChangeListeners();
    }

    //void removeAllChangeListeners()
    public void testFailRemoveAllChangeListeners() {
        Realm realm = getTestRealm();

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        realm.removeAllChangeListeners();
    }

}