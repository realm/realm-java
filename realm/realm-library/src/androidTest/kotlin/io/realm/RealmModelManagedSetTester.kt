/*
 * Copyright 2021 Realm Inc.
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

import io.realm.entities.AllTypes
import io.realm.entities.DogPrimaryKey
import io.realm.entities.SetContainerClass
import io.realm.entities.StringOnly
import io.realm.kotlin.createObject
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for Realm models types of managed sets.
 */
class RealmModelManagedSetTester<T : RealmModel>(
        private val testerName: String,
        private val mixedType: MixedType? = null,
        private val setGetter: KFunction1<AllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<AllTypes, RealmSet<T>, Unit>,
        private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
        private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
        private val initializedSet: List<T?>,
        private val notPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>
) : SetTester {

    private lateinit var tester: ManagedSetTester<T>
    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = when (mixedType) {
        null -> "RealmModelManagedSet-${testerName}"
        else -> "RealmModelManagedSet-${testerName}" + mixedType.name.let { "-$it" }
    }

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)

        realm.executeTransaction {
            this.tester = ManagedSetTester (
                    testerName = testerName,
                    mixedType = mixedType,
                    setGetter = setGetter,
                    setSetter = setSetter,
                    managedSetGetter = managedSetGetter,
                    managedCollectionGetter = managedCollectionGetter,
                    initializedSet = realm.copyToRealmOrUpdate(initializedSet),
                    notPresentValue = realm.copyToRealmOrUpdate(notPresentValue),
                    toArrayManaged = toArrayManaged
            )
        }

        this.tester.setUp(config, looperThread)

        this.realm.close()
    }

    override fun tearDown() = tester.tearDown()

    override fun isManaged() = tester.isManaged()

    override fun isValid() = tester.isValid()

    override fun isFrozen() = tester.isFrozen()

    override fun size() = tester.size()

    override fun isEmpty() = tester.isEmpty()

    override fun contains() = tester.contains()

    override fun iterator() = tester.iterator()

    override fun toArray() = tester.toArray()

    override fun toArrayWithParameter()  = tester.toArrayWithParameter()

    override fun add() = tester.add()

    override fun remove() = tester.remove()

    override fun containsAll() = tester.containsAll()

    override fun addAll() = tester.addAll()

    override fun retainAll() = tester.retainAll()

    override fun removeAll() = tester.removeAll()

    override fun clear() = tester.clear()

    override fun freeze() = tester.freeze()
}
