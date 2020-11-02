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

package io.realm.examples.coroutinesexample.ui.newsreader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dropbox.android.external.store4.StoreResponse
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.databinding.FragmentNewsReaderBinding
import io.realm.examples.coroutinesexample.domain.newsreader.NYTMapper.toDomainArticles
import io.realm.examples.coroutinesexample.ui.dog.DogFragment
import io.realm.examples.coroutinesexample.ui.dog.DomainDogAdapter

class NewsReaderFragment : Fragment() {

    private val viewModel: NewsReaderViewModel by viewModels()
    private val newsReaderAdapter = DomainNewsReaderAdapter()

    private lateinit var binding: FragmentNewsReaderBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = FragmentNewsReaderBinding.inflate(inflater, container, false)
            .also { binding ->
                this.binding = binding
                setupRecyclerView()
                setupLiveData()
            }.root

    private fun setupRecyclerView() {
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = newsReaderAdapter
        }

        with(binding.refresh) {
            setOnRefreshListener {
                viewModel.refreshTopStories()
            }
        }
    }

    private fun setupLiveData() {
        viewModel.storeResponse.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is StoreResponse.Loading -> NewsReaderFragment.StateHelper.loading(binding)
                is StoreResponse.Data -> NewsReaderFragment.StateHelper.data(binding, response, newsReaderAdapter)
                is StoreResponse.NoNewData -> NewsReaderFragment.StateHelper.noNewData(binding)
                is StoreResponse.Error.Exception -> NewsReaderFragment.StateHelper.errorException(binding, response.error)
                is StoreResponse.Error.Message -> NewsReaderFragment.StateHelper.errorMessage(binding, response)
            }
        })
    }

    private object StateHelper {
        fun loading(binding: FragmentNewsReaderBinding) {
            if (!binding.refresh.isRefreshing) {
                binding.refresh.setRefreshing(true)
            }
        }

        fun data(
                binding: FragmentNewsReaderBinding,
                response: StoreResponse.Data<List<RealmNYTimesArticle>>,
                newsReaderAdapter: DomainNewsReaderAdapter
        ) {
            if (binding.refresh.isRefreshing) {
                binding.refresh.setRefreshing(false)
            }
            newsReaderAdapter.submitList(response.value.toDomainArticles())
        }

        fun noNewData(binding: FragmentNewsReaderBinding) {
            // do nothing...?
        }

        fun errorException(binding: FragmentNewsReaderBinding, throwable: Throwable) {
            val stacktrace = throwable.cause?.stackTrace?.joinToString { "$it\n" }
            Log.e(TAG, "--- error (exception): ${throwable.message} - ${throwable.cause?.message}: $stacktrace")
            Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
        }

        fun errorMessage(
                binding: FragmentNewsReaderBinding,
                response: StoreResponse.Error.Message
        ) {
            Log.e(TAG, "--- error (message): ${response.message}")
            Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        fun newInstance() = NewsReaderFragment()
    }
}
