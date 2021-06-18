/**
 * Copyright 2021 Alec Barber.
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
 *
 * TODO: Transfer copyright to Realm Inc.
 */

package io.realm

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.Cat
import io.realm.entities.Dog
import io.realm.entities.DogPrimaryKey
import io.realm.entities.Owner
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class FrozenRealmTests {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        Log.i("FrozenRealmTests", "Realm initialised")
        realmConfiguration = configFactory.createConfigurationBuilder()
            .schema(Dog::class.java, Cat::class.java, DogPrimaryKey::class.java, Owner::class.java)
            .build()
        Log.i("FrozenRealmTests", "Realm configured")
        realm = Realm.getInstance(realmConfiguration)
        Log.i("FrozenRealmTests", "Realm created")
    }

    @After
    fun tearDown() {
        realm.close()
        assertEquals(Realm.getGlobalInstanceCount(realmConfiguration), 0)
    }

    @Test
    fun freezeTwice_closeTwice_noModification() {
        // Freeze the realm twice at the same version
        val realm1 = realm.freeze()
        val realm2 = realm.freeze()

        // If we close one frozen instance, the other instance should be unaffected
        realm2.close()
        assertFalse { realm1.isClosed }
        realm1.close()
        // Check that the only surviving instance is `realm`
        assertEquals(Realm.getGlobalInstanceCount(realmConfiguration), 1)
    }

    @Test
    fun freezeTwice_closeTwice_writeTransaction() {
        // Freeze the realm twice at different versions
        val realm1 = realm.freeze()
        realm.executeTransaction {
            realm.copyToRealm(Dog("Woof", 3))
        }
        val realm2 = realm.freeze()

        // If we close one frozen instance, the other instance should be unaffected
        realm1.close()
        assertFalse { realm2.isClosed }
        realm2.close()
        // Check that the only surviving instance is `realm`
        assertEquals(Realm.getGlobalInstanceCount(realmConfiguration), 1)
    }

    @Test
    fun closeUnderlying_openFrozenRealms() {
        val realm1 = realm.freeze()

        // If we close the last global instance of the live realm, the frozen realm should be closed
        realm.close()
        assertTrue { realm1.isClosed }

        // Check that there are no global instances left
        assertEquals(Realm.getGlobalInstanceCount(realmConfiguration), 0)
    }
}