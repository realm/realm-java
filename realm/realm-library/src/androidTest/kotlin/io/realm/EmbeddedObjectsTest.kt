/*
 * Copyright 2020 Realm Inc.
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
package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.embedded.*
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertFailsWith

/**
 * Class testing the Embedded Objects feature.
 */
@RunWith(AndroidJUnit4::class)
class EmbeddedObjectsTest {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()
    private lateinit var realmConfig: RealmConfiguration
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
    }

    @After
    fun tearDown() {
        if (this::realm.isInitialized) {
            realm.close()
        }
    }

    @Test
    fun queryEmbeddedObjectsThrows() {
        assertFailsWith<IllegalArgumentException> { realm.where<EmbeddedSimpleChild>() }
    }

    @Test
    fun createObject_noParentThrows() {
        // When using createObject, the parent Object must be provided
        realm.beginTransaction()
        assertFailsWith<IllegalArgumentException> { realm.createObject<EmbeddedSimpleChild>() }
    }

    @Test
    fun createObject_throwsIfParentHasMultipleFields() {
        // createObject is an akward API to use for Embedded Objects, so it doesn't support
        // parent objects which has multiple properties linking to it.
        realm.beginTransaction()
        val parent = realm.createObject(EmbeddedTreeParent::class.java, UUID.randomUUID().toString())
        assertFailsWith<IllegalArgumentException> { realm.createObject<EmbeddedTreeNode>(parent) }
    }

    @Test
    fun createObject_simpleSingleChild() {
    }

    @Test
    fun createObject_simpleChildList() {
    }

    @Test
    fun createObject_addToEndOfParentList() {
        // If the only link a parent has to an embedded child is through a list, any new children
        // are added to the end of that list.
        realm.beginTransaction()
        val parent = realm.createObject<EmbeddedSimpleListParent>(UUID.randomUUID().toString())
        parent.children.add(EmbeddedSimpleChild("1"))
        realm.createObject<EmbeddedSimpleChild>(parent)
        assertEquals(2, parent.children.size.toLong())
        assertNotEquals("1", parent.children.last()!!.id)
    }

    @Test
    fun createObject_treeSchema() {
        realm.beginTransaction()


    }

    @Test
    fun createObject_circularSchema() {
    }

    @Test
    fun copyToRealm_noParentThrows() {
    }

    @Test
    fun copyToRealmOrUpdate_throws() {
    }

    @Test
    fun copyToRealm_simpleSingleChild() {
        realm.executeTransaction {
            val parent = EmbeddedSimpleParent("parent1")
            parent.child = EmbeddedSimpleChild("child1")
            it.copyToRealm(parent)
        }

        assertEquals(1, realm.where<EmbeddedSimpleParent>().count())
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun copyToRealm_simpleChildList() {
    }

    @Test
    fun copyToRealm_treeSchema() {
    }

    @Test
    fun copyToRealm_circularSchema() {
    }

    @Test
    fun copyToRealmOrUpdate_deletesOldEmbeddedObject() {
    }

    @Test
    fun insert_noParentThrows() {
    }

    @Test
    fun insertOrUpdate_throws() {
    }

    @Test
    fun insert_simpleSingleChild() {
    }

    @Test
    fun insert_simpleChildList() {
    }

    @Test
    fun insert_treeSchema() {
    }

    @Test
    fun insert_circularSchema() {
    }

    @Test
    fun insertOrUpdate_deletesOldEmbeddedObject() {
    }

    @Test
    fun settingParentFieldDeletesChild() {
    }

    @Test
    fun realmObjectSchema_setEmbedded() {
    }

    @Test
    fun realmObjectSchema_isEmbedded() {
    }

    // Check that deleting a non-embedded parent deletes all embedded children
    @Test
    fun deleteParentObject_deletesEmbeddedChildren() {

    }

    // Check that deleting a embedded parent deletes all embedded children
    @Test
    fun deleteParentEmbeddedObject_deletesEmbeddedChildren() {

    }

    // Cascade deleting an embedded object will trigger its object listener.
    @Test
    fun deleteParent_triggerChildObjectNotifications() {

    }

    // Cascade deleting a parent will trigger the listener on any lists in child embedded
    // objects
    @Test
    fun deleteParent_triggerChildListObjectNotifications() {

    }

    @Test
    fun results_bulkUpdate() {
        // What happens if you bulk update a RealmResults. You should not be allowed to use
        // embedded objects there.
    }
}