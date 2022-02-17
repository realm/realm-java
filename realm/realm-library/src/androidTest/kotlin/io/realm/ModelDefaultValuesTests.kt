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
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import kotlin.test.assertEquals

open class ModelWithPk : RealmObject() {
    @field:PrimaryKey
    var id: Long = 0
}

open class ModelWithoutPk : RealmObject() {
    var id: Long = 0
}

open class ContainerModel : RealmObject() {
    var modelWithPk: ModelWithPk? = ModelWithPk()
    var modelWithoutPk: ModelWithoutPk? = ModelWithoutPk()
}

@RunWith(AndroidJUnit4::class)
class ModelDefaultValuesTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .directory(folder.newFolder())
                .schema(ModelWithPk::class.java,
                        ModelWithoutPk::class.java,
                        ContainerModel::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun defaults_withExistingObjects() {
        realm.executeTransaction {
            realm.createObject<ModelWithoutPk>()
            realm.createObject<ModelWithPk>(0)
        }

        assertEquals(1, realm.where<ModelWithoutPk>().findAll().size)
        assertEquals(1, realm.where<ModelWithPk>().findAll().size)

        realm.executeTransaction {
            realm.createObject<ContainerModel>()
        }

        assertEquals(2, realm.where<ModelWithoutPk>().findAll().size)
        assertEquals(1, realm.where<ModelWithPk>().findAll().size)
        assertEquals(1, realm.where<ContainerModel>().findAll().size)
    }

    @Test
    fun defaults_withNewObjects() {
        realm.executeTransaction {
            realm.createObject<ContainerModel>()
        }

        assertEquals(1, realm.where<ModelWithoutPk>().findAll().size)
        assertEquals(1, realm.where<ModelWithPk>().findAll().size)
        assertEquals(1, realm.where<ContainerModel>().findAll().size)
    }
}