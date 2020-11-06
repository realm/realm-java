//package io.realm.examples.coroutinesexample.data.newsreader.local
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.dropbox.android.external.store4.*
//import io.realm.examples.coroutinesexample.TAG
//import io.realm.examples.coroutinesexample.ui.newsreader.RealmNewsReaderState
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlin.coroutines.coroutineContext
//
//class DataStore(
//        private val dao: RealmNYTDao,
//        private val store: Store<String, List<RealmNYTimesArticle>>
//) {
//
//    private val _newsReaderState = MutableLiveData<RealmNewsReaderState>()
//    val newsReaderState: LiveData<RealmNewsReaderState>
//        get() = _newsReaderState
//
//    private val sectionRefreshJobs = mutableMapOf<String, Job>()
//
//    suspend fun getTopStories(apiSection: String, refresh: Boolean = false) {
//        if (refresh) {
//            store.fresh(apiSection)
//        } else {
//            if (sectionRefreshJobs[apiSection] != null) {
//                getFromCache(apiSection)
//            } else {
//                getFromStream(apiSection)
//            }
//        }
//    }
//
//    fun close() {
//        dao.close()
//    }
//
//    private suspend fun getFromCache(apiSection: String) {
//        val cachedResults = store.get(apiSection)
//        Log.d(TAG, "--- cached data, - '$apiSection': ${cachedResults.size}")
//        _newsReaderState.postValue(RealmNewsReaderState.Data("Cache", cachedResults))
//    }
//
//    private fun getFromStream(apiSection: String) {
//        store.stream(StoreRequest.cached(
//                key = apiSection,
//                refresh = false
//        )).onEach { response ->
//            val origin = response.origin.toString()
//            when (response) {
//                is StoreResponse.Loading -> {
//                    Log.d(TAG, "--- response origin: ${response.origin} - Loading '$apiSection'")
//                    RealmNewsReaderState.Loading(origin)
//                }
//                is StoreResponse.Data -> {
//                    Log.d(TAG, "--- response origin: ${response.origin} - Data '$apiSection': ${response.value.size}")
//                    RealmNewsReaderState.Data(origin, response.value)
//                }
//                is StoreResponse.NoNewData -> {
//                    Log.d(TAG, "--- response origin: ${response.origin} - NoNewData '$apiSection'")
//                    RealmNewsReaderState.NoNewData(origin)
//                }
//                is StoreResponse.Error.Exception -> {
//                    Log.e(TAG, "--- response origin: ${response.origin} - Error.Exception '$apiSection': ${response.error}")
//                    RealmNewsReaderState.ErrorException(origin, response.error)
//                }
//                is StoreResponse.Error.Message -> {
//                    Log.e(TAG, "--- response origin: ${response.origin} - Error.Message '$apiSection': ${response.message}")
//                    RealmNewsReaderState.ErrorMessage(origin, response.message)
//                }
//            }.let {
//                _newsReaderState.postValue(it)
//            }
//        }.launchIn(
//
//        ).also { job ->
//            sectionRefreshJobs[apiSection] = job
//        }
//    }
//}
