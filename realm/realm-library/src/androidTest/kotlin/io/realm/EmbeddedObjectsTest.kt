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
import io.realm.entities.*
import io.realm.entities.embedded.*
import io.realm.kotlin.createEmbeddedObject
import io.realm.kotlin.createObject
import io.realm.kotlin.isValid
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

/**
 * Class testing the Embedded Objects feature.
 */
// FIXME: Move all of these tests out from here. We try to tests by Class, not Feature.
@RunWith(AndroidJUnit4::class)
class EmbeddedObjectsTest {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private val looperThread = BlockingLooperThread()

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
    fun createObject_throwsForEmbeddedClasses() = realm.executeTransaction { realm ->
        assertFailsWith<IllegalArgumentException> { realm.createObject<EmbeddedSimpleChild>() }
    }

    @Test
    fun createObjectWithPrimaryKey_throwsForEmbeddedClasses() = realm.executeTransaction { realm ->
        assertFailsWith<IllegalArgumentException> { realm.createObject<EmbeddedSimpleChild>("foo") }
    }

    @Test
    fun createEmbeddedObject_nullArgsThrows() = realm.executeTransaction { realm ->
        assertFailsWith<IllegalArgumentException> { realm.createEmbeddedObject(EmbeddedSimpleChild::class.java, TestHelper.getNull(), "foo") }
        val parent = realm.createObject<EmbeddedSimpleParent>("parent")
        assertFailsWith<IllegalArgumentException> { realm.createEmbeddedObject(EmbeddedSimpleChild::class.java, parent, TestHelper.getNull()) }
    }

    @Test
    fun createEmbeddedObject_nonExistingParentPropertyNameThrows() = realm.executeTransaction { realm ->
        val parent = realm.createObject<EmbeddedSimpleParent>("parent")
        assertFailsWith<IllegalArgumentException> { realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "foo") }
    }

    @Test
    fun createEmbeddedObject_wrongParentPropertyTypeThrows() = realm.executeTransaction { realm ->
        val parent = realm.createObject<EmbeddedSimpleParent>("parent")

        // TODO: Smoke-test for wrong type. Figure out how to test all unsupported types.
        assertFailsWith<IllegalArgumentException> { realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "id") }
    }

    @Test
    fun createEmbeddedObject_wrongParentPropertyObjectTypeThrows() = realm.executeTransaction { realm ->
        val parent = realm.createObject<EmbeddedSimpleParent>("parent")

        assertFailsWith<IllegalArgumentException> {
            // Embedded object is not of the type the parent object links to.
            realm.createEmbeddedObject<EmbeddedTreeLeaf>(parent, "child")
        }
    }

    @Test
    fun createEmbeddedObject_wrongParentPropertyListTypeThrows() = realm.executeTransaction { realm ->
        val parent = realm.createObject<EmbeddedSimpleListParent>("parent")

        assertFailsWith<IllegalArgumentException> {
            // Embedded object is not of the type the parent object links to.
            realm.createEmbeddedObject<EmbeddedTreeLeaf>(parent, "children")
        }
    }

    @Test
    fun createEmbeddedObject_simpleSingleChild() = realm.executeTransaction { realm ->
        val parent = realm.createObject<EmbeddedSimpleParent>("parent")
        val child = realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "child");
        assertEquals(child.parent, parent)
    }

    @Test
    fun createEmbeddedObject_simpleChildList() = realm.executeTransaction { realm ->
        // Using createEmbeddedObject() with a parent list, will append the object to the end
        // of the list
        val parent = realm.createObject<EmbeddedSimpleListParent>(UUID.randomUUID().toString())
        val child1 = realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "children")
        val child2 = realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "children")
        assertEquals(2, parent.children.size.toLong())
        assertEquals(child1, parent.children.first()!!)
        assertEquals(child2, parent.children.last()!!)
    }

    @Test
    fun dynamicRealm_createEmbeddedObject() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleParent", "PK_VALUE")
                    val child = realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "child")

                    val idValue = "ID_VALUE"
                    child.setString("id", idValue)

                    val childInParent = parent.getObject("child")
                    assertNotNull(childInParent)
                    assertEquals(childInParent!!.getString("id"), idValue)
                    assertEquals(child, childInParent)

                    val linkingParent = child.linkingObjects("EmbeddedSimpleParent", "child")
                            .first()
                    assertNotNull(linkingParent)
                    assertEquals(parent.getString("id"), linkingParent!!.getString("id"))
                    assertEquals(parent.getObject("child"), linkingParent.getObject("child"))
                }
            }

    @Test
    fun dynamicRealm_createEmbeddedObject_simpleChildList() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleListParent", UUID.randomUUID().toString())
                    val child1 = realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "children")
                    val child2 = realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "children")
                    assertEquals(2, parent.getList("children").size.toLong())
                    assertEquals(child1, parent.getList("children").first()!!)
                    assertEquals(child2, parent.getList("children").last()!!)
                }
            }

    @Test
    fun dynamicRealm_createEmbeddedObject_wrongParentPropertyTypeThrows() {
        DynamicRealm.getInstance(realm.configuration).use { realm ->
            realm.executeTransaction {
                val parent = realm.createObject("EmbeddedSimpleParent", "parent")

                // TODO: Smoke-test for wrong type. Figure out how to test all unsupported types.
                assertFailsWith<IllegalArgumentException> { realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "id") }
            }
        }
    }

    @Test
    fun dynamicRealm_createEmbeddedObject_wrongParentPropertyObjectTypeThrows() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleParent", "parent")

                    assertFailsWith<IllegalArgumentException> {
                        // Embedded object is not of the type the parent object links to.
                        realm.createEmbeddedObject("EmbeddedTreeLeaf", parent, "child")
                    }
                }
            }

    @Test
    fun dynamicRealm_createEmbeddedObject_wrongParentPropertyListTypeThrows() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleListParent", "parent")

                    assertFailsWith<IllegalArgumentException> {
                        // Embedded object is not of the type the parent object links to.
                        realm.createEmbeddedObject("EmbeddedTreeLeaf", parent, "children")
                    }
                }
            }

    @Test
    fun settingParentFieldDeletesChild() = realm.executeTransaction { realm ->
        val parent = EmbeddedSimpleParent("parent")
        parent.child = EmbeddedSimpleChild("child")

        val managedParent: EmbeddedSimpleParent = realm.copyToRealm(parent)
        val managedChild: EmbeddedSimpleChild = managedParent.child!!
        managedParent.child = null // Will delete the embedded object
        assertFalse(managedChild.isValid)
        assertEquals(0, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun dynamicRealm_settingParentFieldDeletesChild() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleParent", "parent")
                    val child = realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "child")

                    assertEquals(1, realm.where("EmbeddedSimpleChild").count())
                    parent.setObject("child", null)
                    assertFalse(child.isValid)
                    assertEquals(0, realm.where("EmbeddedSimpleChild").count())
                }
            }

    @Test
    fun objectAccessor_willAutomaticallyCopyUnmanaged() = realm.executeTransaction { realm ->
        // Checks that adding an unmanaged embedded object to a property will automatically copy it.
        val parent = EmbeddedSimpleParent("parent")
        val managedParent: EmbeddedSimpleParent = realm.copyToRealm(parent)

        assertEquals(0, realm.where<EmbeddedSimpleChild>().count())
        managedParent.child = EmbeddedSimpleChild("child") // Will copy the object to Realm
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        assertTrue(managedParent.child!!.isValid)
    }

    @Test
    fun objectAccessor_willAutomaticallyCopyManaged() = realm.executeTransaction { realm ->
        // Checks that setting a link to a managed embedded object will automatically copy it unlike
        // normal objects that allow multiple parents. Note: This behavior is a bit controversial
        // and was subject to a lot of discussion during API design. The problem is that making
        // the behavior explicit will result in an extremely annoying API. We need to carefully
        // monitor if people understand how this behaves.
        val managedParent1: EmbeddedSimpleParent = realm.copyToRealm(EmbeddedSimpleParent("parent1"))
        val managedParent2: EmbeddedSimpleParent = realm.copyToRealm(EmbeddedSimpleParent("parent2"))

        assertEquals(0, realm.where<EmbeddedSimpleChild>().count())
        managedParent1.child = EmbeddedSimpleChild("child")
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        managedParent2.child = managedParent1.child // Will copy the embedded object
        assertEquals(2, realm.where<EmbeddedSimpleChild>().count())
        assertNotEquals(managedParent1.child, managedParent2.child)
    }

    @Test
    fun objectAccessor_willCopyUnderConstruction() = realm.executeTransaction { realm ->
        val unmanagedObj = EmbeddedWithConstructorArgs()
        val managedObj = realm.copyToRealm(unmanagedObj)
        assertEquals(EmbeddedWithConstructorArgs.INNER_CHILD_ID, managedObj.child!!.id)
    }

    @Test
    fun realmList_add_willAutomaticallyCopy() = realm.executeTransaction { realm ->
        val parent = realm.copyToRealm(EmbeddedSimpleListParent("parent"))
        assertTrue(parent.children.add(EmbeddedSimpleChild("child")))
        val child = parent.children.first()!!
        assertTrue(child.isValid)
        assertEquals("child", child.id)

        // FIXME: How to handle DynamicRealmObject :(
    }

    @Test
    fun realmList_addIndex_willAutomaticallyCopy() = realm.executeTransaction { realm ->
        val parent = realm.copyToRealm(EmbeddedSimpleListParent("parent"))
        parent.children.add(EmbeddedSimpleChild("secondChild"))
        parent.children.add(0, EmbeddedSimpleChild("firstChild"))
        val child = parent.children.first()!!
        assertTrue(child.isValid)
        assertEquals("firstChild", child.id)

        // FIXME: How to handle DynamicRealmObject :(
    }

    @Test
    fun realmList_set_willAutomaticallyCopy() = realm.executeTransaction { realm ->
        // Checks that adding an unmanaged embedded object to a list will automatically make
        // it managed
        val parent = realm.copyToRealm(EmbeddedSimpleListParent("parent"))
        assertTrue(parent.children.add(EmbeddedSimpleChild("child")))
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        parent.children[0] = EmbeddedSimpleChild("OtherChild")
        assertEquals("OtherChild", parent.children.first()!!.id)
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())

        // FIXME: How to handle DynamicRealmObject :(
    }

    @Test
    fun copyToRealm_noParentThrows() = realm.executeTransaction {
        assertFailsWith<IllegalArgumentException> {
            realm.copyToRealm(EmbeddedSimpleChild("child"))
        }
    }

    @Test
    fun copyToRealmOrUpdate_NoParentThrows() = realm.executeTransaction {
        assertFailsWith<IllegalArgumentException> {
            realm.copyToRealmOrUpdate(EmbeddedSimpleChild("child"))
        }
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
        realm.executeTransaction {
            val parent = EmbeddedSimpleListParent("parent1")
            parent.children = RealmList(EmbeddedSimpleChild("child1"))
            it.copyToRealm(parent)
        }

        assertEquals(1, realm.where<EmbeddedSimpleListParent>().count())
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun copyToRealm_treeSchema() {
        realm.executeTransaction {
            val parent = EmbeddedTreeParent("parent1")

            val node1 = EmbeddedTreeNode("node1")
            node1.leafNode = EmbeddedTreeLeaf("leaf1")
            parent.middleNode = node1
            val node2 = EmbeddedTreeNode("node2")
            node2.leafNodeList.add(EmbeddedTreeLeaf("leaf2"))
            node2.leafNodeList.add(EmbeddedTreeLeaf("leaf3"))
            parent.middleNodeList.add(node2)

            it.copyToRealm(parent)
        }

        assertEquals(1, realm.where<EmbeddedTreeParent>().count())
        assertEquals("parent1", realm.where<EmbeddedTreeParent>().findFirst()!!.id)

        assertEquals(2, realm.where<EmbeddedTreeNode>().count())
        val nodeResults = realm.where<EmbeddedTreeNode>().findAll()
        assertTrue(nodeResults.any { it.id == "node1" })
        assertTrue(nodeResults.any { it.id == "node2" })

        assertEquals(3, realm.where<EmbeddedTreeLeaf>().count())
        val leafResults = realm.where<EmbeddedTreeLeaf>().findAll()
        assertTrue(leafResults.any { it.id == "leaf1" })
        assertTrue(leafResults.any { it.id == "leaf2" })
        assertTrue(leafResults.any { it.id == "leaf3" })
    }

    @Test
    fun copyToRealm_circularSchema() {
        realm.executeTransaction {
            val parent = EmbeddedCircularParent("parent")
            val child1 = EmbeddedCircularChild("child1")
            val child2 = EmbeddedCircularChild("child2")
            child1.singleChild = child2
            parent.singleChild = child1
            it.copyToRealm(parent)
        }

        assertEquals(1, realm.where<EmbeddedCircularParent>().count())
        assertEquals(2, realm.where<EmbeddedCircularChild>().count())
    }

    @Test
    fun copyToRealm_throwsIfMultipleRefsToSingleObjectsExists() {
        realm.executeTransaction { r ->
            val parent = EmbeddedCircularParent("parent")
            val child = EmbeddedCircularChild("child")
            child.singleChild = child // Create circle between children
            parent.singleChild = child
            assertFailsWith<IllegalArgumentException> { r.copyToRealm(parent) }
        }
    }

    @Test
    fun copyToRealm_throwsIfMultipleRefsToListObjectsExists() {
        realm.executeTransaction { r ->
            val parent = EmbeddedSimpleListParent("parent")
            val child = EmbeddedSimpleChild("child")
            parent.children = RealmList(child, child)
            assertFailsWith<IllegalArgumentException> { r.copyToRealm(parent) }
        }
    }

    @Test
    @Ignore("FIXME")
    fun copyToRealmOrUpdate_deleteReplacedObjects() {
        TODO()
    }

    @Test
    fun insert_noParentThrows() {
        realm.executeTransaction { realm ->
            val child = EmbeddedSimpleChild("child")
            assertFailsWith<IllegalArgumentException> { realm.insert(child) }
        }
    }

    @Test
    fun dynamicRealm_createEmbeddedObjectWithUnmanagedParentThrows() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = EmbeddedSimpleParent("parent")
                    assertFailsWith<IllegalArgumentException> {
                        it.createEmbeddedObject("EmbeddedSimpleChild", parent, "child")
                    }
                }
            }

    @Test
    @Ignore("Add in another PR")
    fun insertOrUpdate_throws() {
        TODO()
    }

    @Test
    fun insert_simpleSingleChild() {
        realm.executeTransaction {
            val parent = EmbeddedSimpleParent("parent1")
            parent.child = EmbeddedSimpleChild("child1")
            it.insert(parent)
        }

        assertEquals(1, realm.where<EmbeddedSimpleParent>().count())
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun insert_simpleChildList() {
        realm.executeTransaction {
            val parent = EmbeddedSimpleListParent("parent1")
            parent.children = RealmList(EmbeddedSimpleChild("child1"))
            it.insert(parent)
        }

        assertEquals(1, realm.where<EmbeddedSimpleListParent>().count())
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun insert_treeSchema() {
        realm.executeTransaction {
            val parent = EmbeddedTreeParent("parent1")

            val node1 = EmbeddedTreeNode("node1")
            node1.leafNode = EmbeddedTreeLeaf("leaf1")
            parent.middleNode = node1
            val node2 = EmbeddedTreeNode("node2")
            node2.leafNodeList.add(EmbeddedTreeLeaf("leaf2"))
            node2.leafNodeList.add(EmbeddedTreeLeaf("leaf3"))
            parent.middleNodeList.add(node2)

            it.insert(parent)
        }

        assertEquals(1, realm.where<EmbeddedTreeParent>().count())
        assertEquals("parent1", realm.where<EmbeddedTreeParent>().findFirst()!!.id)

        assertEquals(2, realm.where<EmbeddedTreeNode>().count())
        val nodeResults = realm.where<EmbeddedTreeNode>().findAll()
        assertTrue(nodeResults.any { it.id == "node1" })
        assertTrue(nodeResults.any { it.id == "node2" })

        assertEquals(3, realm.where<EmbeddedTreeLeaf>().count())
        val leafResults = realm.where<EmbeddedTreeLeaf>().findAll()
        assertTrue(leafResults.any { it.id == "leaf1" })
        assertTrue(leafResults.any { it.id == "leaf2" })
        assertTrue(leafResults.any { it.id == "leaf3" })
    }

    @Test
    fun insert_circularSchema() {
        realm.executeTransaction {
            val parent = EmbeddedCircularParent("parent")
            val child1 = EmbeddedCircularChild("child1")
            val child2 = EmbeddedCircularChild("child2")
            child1.singleChild = child2
            parent.singleChild = child1
            it.insert(parent)
        }

        assertEquals(1, realm.where<EmbeddedCircularParent>().count())
        assertEquals(2, realm.where<EmbeddedCircularChild>().count())
    }

    @Test
    @Ignore("Add in another PR")
    fun insertOrUpdate_deletesOldEmbeddedObject() {
        TODO()
    }

    @Test
    @Ignore("Add in another PR")
    fun insert_listWithEmbeddedObjects() {
        TODO()
    }

    @Test
    @Ignore("Add in another PR")
    fun insertOrUpdate_listWithEmbeddedObjects() {
        TODO()
    }

    @Test
    fun createEmbeddedObjectFromJson() {
        realm.executeTransaction { realm ->
            realm.createObjectFromJson(EmbeddedCircularParent::class.java, """
                        { 
                            "id": "uuid", 
                            "singleChild": { 
                                "id" : "childId", 
                                "singleChild" : { 
                                    "id": "embeddedChildId" 
                                }
                            }
                        }
                """)
        }
        val circularParent = realm.where(EmbeddedCircularParent::class.java).findFirst()!!
        val singleChild = circularParent.singleChild!!
        assertEquals("childId", singleChild.id)
        assertEquals("embeddedChildId", singleChild.singleChild!!.id)
    }

    @Test
    fun createEmbeddedObjectListElementFromJson() {
        realm.executeTransaction { realm ->
            realm.createObjectFromJson(EmbeddedSimpleListParent::class.java, """
                        { 
                            "id": "uuid", 
                            "children": [
                                { "id" : "child1" },
                                { "id" : "child2" },
                                { "id" : "child3" }
                            ]
                        }
                """)
        }
        val parent = realm.where(EmbeddedSimpleListParent::class.java).findFirst()!!
        assertEquals(3, parent.children!!.count())
        assertEquals("child1", parent.children[0]!!.id)
        assertEquals("child2", parent.children[1]!!.id)
        assertEquals("child3", parent.children[2]!!.id)
    }

    @Test
    fun createOrphanedEmbeddedObjectFromJsonThrows() {
        assertFailsWith<IllegalArgumentException> {
            realm.executeTransaction { realm ->
                realm.createObjectFromJson(EmbeddedSimpleChild::class.java, """ {"id": "uuid" } """ )
            }
        }
    }

    @Test
    @Ignore("FIXME Not implemented yet")
    fun createEmbeddedObjectFromJson_streamBased() { }

    @Test
    fun realmObjectSchema_setEmbedded() {
        DynamicRealm.getInstance(realm.configuration).use { realm ->
            realm.executeTransaction {
                val objSchema: RealmObjectSchema = realm.schema[EmbeddedSimpleChild.NAME]!!
                assertTrue(objSchema.isEmbedded)
                objSchema.isEmbedded = false
                assertFalse(objSchema.isEmbedded)
                objSchema.isEmbedded = true
                assertTrue(objSchema.isEmbedded)
            }
        }
    }

    @Test
    fun realmObjectSchema_setEmbedded_throwsWithPrimaryKey() {
        DynamicRealm.getInstance(realm.configuration).use { realm ->
            realm.executeTransaction {
                val objSchema: RealmObjectSchema = realm.schema[AllJavaTypes.CLASS_NAME]!!
                assertFailsWith<IllegalStateException> { objSchema.isEmbedded = true }
            }
        }
    }

    @Test
    fun realmObjectSchema_setEmbedded_throwsIfBreaksParentInvariants() {
        // Classes can only be converted to be embedded if all objects have exactly one other
        // object pointing to it.
        DynamicRealm.getInstance(realm.configuration).use { realm ->
            realm.executeTransaction {

                // Create object with no parents
                realm.createObject(Dog.CLASS_NAME)
                val dogSchema = realm.schema[Dog.CLASS_NAME]!!
                // Succeed by mistake right now.
                // See https://github.com/realm/realm-core/issues/3729
                // The correct check is just below
                dogSchema.isEmbedded = true
                // assertFailsWith<IllegalStateException> {
                //    dogSchema.isEmbedded = true
                // }

                // Create object with two parents
                val cat: DynamicRealmObject = realm.createObject(Cat.CLASS_NAME)
                val owner1: DynamicRealmObject = realm.createObject(Owner.CLASS_NAME)
                owner1.setObject(Owner.FIELD_CAT, cat)
                val owner2: DynamicRealmObject = realm.createObject(Owner.CLASS_NAME)
                owner2.setObject(Owner.FIELD_CAT, cat)
                val catSchema = realm.schema[Cat.CLASS_NAME]!!
                assertFailsWith<IllegalStateException> {
                    catSchema.isEmbedded = true
                }
            }
        }
    }

    @Test
    fun realmObjectSchema_isEmbedded() {
        assertTrue(realm.schema[EmbeddedSimpleChild.NAME]!!.isEmbedded)
        assertFalse(realm.schema[AllTypes.CLASS_NAME]!!.isEmbedded)
    }

    @Test
    fun dynamicRealm_realmObjectSchema_isEmbedded() {
        DynamicRealm.getInstance(realm.configuration).use { realm ->
            assertTrue(realm.schema[EmbeddedSimpleChild.NAME]!!.isEmbedded)
            assertFalse(realm.schema[AllTypes.CLASS_NAME]!!.isEmbedded)
        }
    }

    // Check that deleting a non-embedded parent deletes all embedded children
    @Test
    fun deleteParentObject_deletesEmbeddedChildren() = realm.executeTransaction {
        val parent = EmbeddedSimpleParent("parent")
        parent.child = EmbeddedSimpleChild("child")

        val managedParent: EmbeddedSimpleParent = it.copyToRealm(parent)
        assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        val managedChild: EmbeddedSimpleChild = managedParent.child!!

        managedParent.deleteFromRealm()
        assertFalse(managedChild.isValid)
        assertEquals(0, realm.where<EmbeddedSimpleParent>().count())
        assertEquals(0, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun dynamicRealm_deleteParentObject_deletesEmbeddedChildren() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedSimpleParent", "parent")
                    assertEquals(0, realm.where("EmbeddedSimpleChild").count())

                    val child = realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "child")
                    assertEquals(1, realm.where("EmbeddedSimpleChild").count())

                    parent.deleteFromRealm()
                    assertFalse(child.isValid)
                    assertEquals(0, realm.where("EmbeddedSimpleParent").count())
                    assertEquals(0, realm.where("EmbeddedSimpleChild").count())
                }
            }

    // Check that deleting a embedded parent deletes all embedded children
    @Test
    fun deleteParentEmbeddedObject_deletesEmbeddedChildren() = realm.executeTransaction {
        val parent = EmbeddedTreeParent("parent1")
        val middleNode = EmbeddedTreeNode("node1")
        middleNode.leafNode = EmbeddedTreeLeaf("leaf1")
        middleNode.leafNodeList.add(EmbeddedTreeLeaf("leaf2"))
        middleNode.leafNodeList.add(EmbeddedTreeLeaf("leaf3"))
        parent.middleNode = middleNode

        val managedParent: EmbeddedTreeParent = it.copyToRealm(parent)
        assertEquals(1, realm.where<EmbeddedTreeNode>().count())
        assertEquals(3, realm.where<EmbeddedTreeLeaf>().count())
        managedParent.deleteFromRealm()
        assertEquals(0, realm.where<EmbeddedTreeNode>().count())
        assertEquals(0, realm.where<EmbeddedSimpleChild>().count())
    }

    @Test
    fun dynamic_deleteParentEmbeddedObject_deletesEmbeddedChildren() =
            DynamicRealm.getInstance(realm.configuration).use { realm ->
                realm.executeTransaction {
                    val parent = realm.createObject("EmbeddedTreeParent", "parent1")
                    val middleNode = realm.createEmbeddedObject("EmbeddedTreeNode", parent, "middleNode");
                    middleNode.setString("id", "node1")
                    val leaf1 = realm.createEmbeddedObject("EmbeddedTreeLeaf", middleNode, "leafNode");
                    val leaf2 = realm.createEmbeddedObject("EmbeddedTreeLeaf", middleNode, "leafNodeList");
                    val leaf3 = realm.createEmbeddedObject("EmbeddedTreeLeaf", middleNode, "leafNodeList");

                    assertEquals(1, realm.where("EmbeddedTreeNode").count())
                    assertEquals(3, realm.where("EmbeddedTreeLeaf").count())
                    parent.deleteFromRealm()
                    assertEquals(0, realm.where("EmbeddedTreeNode").count())
                    assertEquals(0, realm.where("EmbeddedSimpleChild").count())
                    assertFalse(parent.isValid)
                    assertFalse(middleNode.isValid)
                    assertFalse(leaf1.isValid)
                    assertFalse(leaf2.isValid)
                    assertFalse(leaf3.isValid)
                }
            }

    // Cascade deleting an embedded object will trigger its object listener.
    @Test
    fun deleteParent_triggerChildObjectNotifications() = looperThread.runBlocking {
        val realm = Realm.getInstance(realm.configuration)
        looperThread.closeAfterTest(realm)

        realm.executeTransaction {
            val parent = EmbeddedSimpleParent("parent")
            val child = EmbeddedSimpleChild("child")
            parent.child = child
            it.copyToRealm(parent)
        }

        val child = realm.where<EmbeddedSimpleParent>().findFirst()!!.child!!
        child.addChangeListener(RealmChangeListener<EmbeddedSimpleChild> {
            if (!it.isValid) {
                looperThread.testComplete()
            }
        })

        realm.executeTransaction {
            child.parent.deleteFromRealm()
        }
    }

    @Test
    fun dynamicRealm_deleteParent_triggerChildObjectNotifications() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(realm.configuration)
        looperThread.closeAfterTest(realm)

        realm.executeTransaction {
            val parent = realm.createObject("EmbeddedSimpleParent", "parent")
            realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "child")
                    .apply { setString("id", "child") }
        }

        val queriedChild = realm.where("EmbeddedSimpleParent")
                .findFirst()!!
                .getObject("child")!!
                .apply {
                    addChangeListener(RealmChangeListener<DynamicRealmObject> {
                        if (!it.isValid) {
                            looperThread.testComplete()
                        }
                    })
                }

        realm.executeTransaction {
            queriedChild.linkingObjects("EmbeddedSimpleParent", "child")
                    .first()!!
                    .deleteFromRealm()
        }
    }

    // Cascade deleting a parent will trigger the listener on any lists in child embedded
    // objects
    @Test
    fun deleteParent_triggerChildListObjectNotifications() = looperThread.runBlocking {
        val realm = Realm.getInstance(realm.configuration)
        looperThread.closeAfterTest(realm)

        realm.executeTransaction {
            val parent = EmbeddedSimpleListParent("parent")
            val child1 = EmbeddedSimpleChild("child1")
            val child2 = EmbeddedSimpleChild("child2")
            parent.children.add(child1)
            parent.children.add(child2)
            it.copyToRealm(parent)
        }

        val children: RealmList<EmbeddedSimpleChild> = realm.where<EmbeddedSimpleListParent>()
                .findFirst()!!
                .children

        children.addChangeListener { list ->
            if (!list.isValid) {
                looperThread.testComplete()
            }
        }

        realm.executeTransaction {
            realm.where<EmbeddedSimpleListParent>().findFirst()!!.deleteFromRealm()
        }
    }

    @Test
    fun dynamicRealm_deleteParent_triggerChildListObjectNotifications() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(realm.configuration)
        looperThread.closeAfterTest(realm)

        realm.executeTransaction {
            val parent = realm.createObject("EmbeddedSimpleListParent", "parent")
            realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "children")
                    .apply { setString("id", "child1") }
            realm.createEmbeddedObject("EmbeddedSimpleChild", parent, "children")
                    .apply { setString("id", "child2") }
        }

        realm.where("EmbeddedSimpleListParent")
                .findFirst()!!
                .getList("children")
                .apply {
                    addChangeListener { list ->
                        if (!list.isValid) {
                            looperThread.testComplete()
                        }
                    }
                }

        realm.executeTransaction {
            realm.where("EmbeddedSimpleListParent")
                    .findFirst()!!
                    .deleteFromRealm()
        }
    }

    @Test
    @Ignore("Add in another PR")
    fun results_bulkUpdate() {
        // What happens if you bulk update a RealmResults. Should it be allowed to use embeded
        // objects here?
        TODO()
    }
}
