package io.realm.examples.coroutinesexample.repository

import android.util.Log
import io.realm.Realm
import io.realm.examples.coroutinesexample.model.Doggo
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun insertDogs()
    fun getDogs(): Flow<List<Doggo>>
    fun deleteDogs()
}

class RealmRepository : Repository {

    override suspend fun insertDogs() {
        Realm.getDefaultInstance().use { defaultRealm ->
            defaultRealm.executeTransactionAwait { realm ->
                Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE  transaction")
                for (i in 1..10) {
                    realm.insertOrUpdate(Doggo().apply {
                        name = "Fluffy $i"
                        age = 1
                        owner = "Jane Doe"
                    })
                }
                Log.e("--->", "---> ${Thread.currentThread().name} - AFTER   transaction")
            }
        }
    }

    override fun getDogs(): Flow<List<Doggo>> {
        return Realm.getDefaultInstance()
                .where(Doggo::class.java)
                .findAllAsync()
                .toFlow()
    }

    override fun deleteDogs() {
        Realm.getDefaultInstance().use { defaultRealm ->
            defaultRealm.executeTransaction { realm ->
                realm.deleteAll()
            }
        }
    }
}
