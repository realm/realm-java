package io.realm.examples.coroutinesexample.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.realm.examples.coroutinesexample.model.Doggo
import io.realm.examples.coroutinesexample.repository.RealmRepository
import io.realm.examples.coroutinesexample.repository.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository: Repository = RealmRepository()

    fun insertDogs() {
        viewModelScope.launch {
            Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE  insertDogs")
            repository.insertDogs()
            Log.e("--->", "---> ${Thread.currentThread().name} - AFTER   insertDogs")
        }
    }

    fun getDogs(): LiveData<List<Doggo>> {
        return liveData {
            repository.getDogs()
                    .collect {
                        Log.e("--->", "---> ${Thread.currentThread().name} - collect!")
                        emit(it)
                    }
        }
    }

    fun deleteAll() {
        Log.e("--->", "---> ${Thread.currentThread().name} - BEFORE deleteAll")
        repository.deleteDogs()
        Log.e("--->", "---> ${Thread.currentThread().name} - AFTER  deleteAll")
    }
}
