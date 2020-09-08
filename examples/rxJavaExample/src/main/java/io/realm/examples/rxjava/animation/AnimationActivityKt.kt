/*
 * Copyright 2015 Realm Inc.
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
package io.realm.examples.rxjava.animation

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.examples.rxjava.R
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.*

class AnimationActivityKt : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private val scope = CoroutineScope(Dispatchers.Main)

    private var realm: Realm? = null
    private var container: ViewGroup? = null

    private val viewModel: AnimationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animations)
        container = findViewById(R.id.list)
//        realm = Realm.getDefaultInstance()
    }

    override fun onResume() {
        super.onResume()

        // Load all persons and start inserting them with 1 sec. intervals.
        // All RealmObject access has to be done on the same thread `findAllAsync` was called on.
        // Warning: This example doesn't handle back pressure well.
        scope.launch {
            realm = Realm.getDefaultInstance()
//            realm!!.where(Person::class.java)
//                    .findAll()
//                    .toFlow(scope)
//                    .flatMapMerge { results: RealmResults<Person> ->
//                        results.asFlow()    // emit each person individually
//                    }.onEach { delay(1000) }
//                    .collect { person: Person ->
//                        Log.e("--->", "---> $person")
//                        val personView = TextView(this@AnimationActivityKt)
//                        personView.text = person.name
//                        container!!.addView(personView)
//                    }
        }

//        disposable.add(realm!!.where(Person::class.java)
//                .findAllAsync()
//                .asFlowable()
//                .flatMap { persons: RealmResults<Person>? -> Flowable.fromIterable(persons) }
//                .zipWith(Flowable.interval(1, TimeUnit.SECONDS), BiFunction { person: Person, tick: Long? -> person })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { person: Person ->
//                    val personView = TextView(this@AnimationActivityKt)
//                    personView.text = person.name
//                    container!!.addView(personView)
//                })

//        viewModel.getData().observe(this, Observer {
//            Log.e("--->", "---> $it")
//        })

        Realm.getDefaultInstance().executeTransaction { realm ->
            realm.deleteAll()
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            Realm.getDefaultInstance().use { realm ->
                Log.e("--->", "---> doggos: ${realm.where(Doggo::class.java).count()}")
            }
        }
        viewModel.doStuff()
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
        scope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm!!.close()
    }
}

class AnimationViewModel : ViewModel() {

    private val repo = Repo()

    fun doStuff() {
        viewModelScope.launch {
            Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE  doStuff")
            repo.doStuff()
            Log.e("--->", "---> ${Thread.currentThread().name} - AFTER   doStuff")
        }
    }

    fun getData(): LiveData<String> {
        return liveData {
            Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE  getData")
            val value = repo.getData()
            Log.e("--->", "---> ${Thread.currentThread().name} - AFTER   getData")
            emit(value)
        }
    }
}

class Repo {
    suspend fun doStuff() {
        Realm.getDefaultInstance().use { defaultRealm ->
            defaultRealm.executeTransactionAwait { realm ->
                Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE transaction")
                Thread.sleep(5000)
                realm.insertOrUpdate(Doggo().apply {
                    name = "Fluffy"
                    age = 1
                    owner = "Jane Doe"
                })
                Log.e("--->", "---> ${Thread.currentThread().name} - AFTER  transaction")
            }
        }
    }

    suspend fun getData(): String {
        delay(5000)
        return "OMG"
    }
}

open class Doggo : RealmObject() {
    @PrimaryKey
    var name: String = ""
    var age: Int = 1
    var owner: String = ""
}
