/*
 * Copyright 2019 Realm Inc.
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

package io.realm.examples.objectserver.activitytracker.ui.orderlist

import android.os.HandlerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.recyclerview.widget.DiffUtil
import io.realm.examples.objectserver.activitytracker.model.App
import io.realm.examples.objectserver.activitytracker.model.entities.Order
import io.realm.examples.objectserver.activitytracker.ui.BaseViewModel
import io.realm.examples.objectserver.activitytracker.ui.shared.createObservableForRealm
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.realm.OrderedCollectionChangeSet
import io.realm.kotlin.where
import java.util.*

class OrdersViewModel: BaseViewModel() {

    private val worker1 = HandlerThread("HandlerWorker1")
    private val worker2 = HandlerThread("HandlerWorker2")

    init {
        worker1.start()
        worker2.start()
    }

    fun createOrder() {
        realm.executeTransactionAsync {
            val o = Order()
            o.name = "Custom drink"
            o.from = "Realm1"
            o.createdAt = Date(Random().nextInt(1000).toLong())
            it.insertOrUpdate(o)
        }
    }

    fun orders(): LiveData<Pair<List<Order>, DiffUtil.DiffResult?>> {
        // Create Streams from each Realm.
        //
        // Note especially the special `createObservableForRealm` which is required to manage
        // the lifecycle of the Realm instance within the stream
        // Also, be careful how results from this Realm are used. In our case we keep all
        // managed results on the same thread until they have been copied out, after which they
        // can be observed on other threads without issues.
        //
        // We ignore the INITIAL state as that is mostly used for checking the Subscription state
        // which isn't required here. So we just wait for any UPDATE state which will follow
        // immediately after as the Subscription is either created or updated. This way we also
        // skip doing a lof of extra work in the streams not needed.
        val worker1Looper = AndroidSchedulers.from(worker1.looper)
        val realm1Orders = Flowable.just(App.REALM1_CONFIG)
                .observeOn(worker1Looper)
                .flatMap { createObservableForRealm(it) }
                .flatMap { it.where<Order>().findAllAsync().asChangesetObservable().toFlowable(BackpressureStrategy.ERROR) }
                .filter { it.changeset?.state != OrderedCollectionChangeSet.State.INITIAL }
                .filter { it.collection.isLoaded }
                .map { it.collection.realm.copyFromRealm(it.collection) }
                .unsubscribeOn(worker1Looper)

        val worker2Looper = AndroidSchedulers.from(worker2.looper)
        val realm2Orders = Flowable.just(App.REALM2_CONFIG)
                .observeOn(worker2Looper)
                .flatMap { createObservableForRealm(it) }
                .flatMap { it.where<Order>().findAllAsync().asChangesetObservable().toFlowable(BackpressureStrategy.ERROR) }
                .filter { it.changeset?.state != OrderedCollectionChangeSet.State.INITIAL }
                .filter { it.collection.isLoaded }
                .map { it.collection.realm.copyFromRealm(it.collection) }
                .unsubscribeOn(worker2Looper)

        // Merge the two Realm streams and calculate the combined fine-grained animations using
        // DiffUtil.
        // Inspired by https://hellsoft.se/a-nice-combination-of-rxjava-and-diffutil-fe3807186012
        val initialPair: Pair<List<Order>, DiffUtil.DiffResult?> = Pair(listOf(), null)
        val mergedEntries = Flowable.combineLatest(realm1Orders, realm2Orders, BiFunction<List<Order>, List<Order>, Pair<List<Order>, List<Order>>> { orders1, orders2 ->
                    // Do minimal amount of of work needed to package the lists up so they can be
                    // used on a background thread.
                    Pair(orders1, orders2);
                })
                .observeOn(Schedulers.computation())
                .map {
                    // Merge the two lists and sort them according to timestamp in a natural order
                    ArrayList<Order>()
                            .apply {
                                addAll(it.first)
                                addAll(it.second)
                            }
                            .sortedWith(Comparator { first, second ->
                                first.createdAt.compareTo(second.createdAt)
                            })
                }
                .scan(initialPair) { pair, next ->
                    // Calculate the DiffUtil results
                    val callback = MyDiffCallback(pair.first, next)
                    val result: DiffUtil.DiffResult = DiffUtil.calculateDiff(callback);
                    Pair(next, result);
                }
                .skip(1)
                .subscribeOn(AndroidSchedulers.mainThread())

        return LiveDataReactiveStreams.fromPublisher(mergedEntries)
    }

    override fun onCleared() {
        super.onCleared()
        worker1.quit()
        worker2.quit()
    }

    private class MyDiffCallback(private val current: List<Order>, private val next: List<Order>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return current.size
        }

        override fun getNewListSize(): Int {
            return next.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val currentItem = current[oldItemPosition]
            val nextItem = next[newItemPosition]
            return currentItem.id == nextItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val currentItem = current[oldItemPosition]
            val nextItem = next[newItemPosition]
            return currentItem == nextItem
        }
    }
}