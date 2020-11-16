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

package io.realm.examples.coroutinesexample.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.network.sectionsToNames
import io.realm.examples.coroutinesexample.databinding.FragmentMainBinding
import java.util.*
import kotlin.Comparator

class MainFragment : Fragment() {

    interface OnItemClicked {
        fun onItemClicked(id: String)
    }

    internal lateinit var onItemclickedCallback: OnItemClicked

    private val viewModel: MainViewModel by viewModels()
    private val newsReaderAdapter = MainAdapter { id ->
        onItemclickedCallback.onItemClicked(id)
    }

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = FragmentMainBinding.inflate(inflater, container, false)
            .also { binding ->
                binding.lifecycleOwner = viewLifecycleOwner
                this.binding = binding
                setupSpinner()
                setupRecyclerView()
                setupLiveData()
            }.root

    private fun setupSpinner() {
        with(binding.spinner) {
            adapter = ArrayAdapter<CharSequence>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    sectionsToNames.keys.sortedWith(
                            Comparator { o1, o2 ->
                                if (o1.toLowerCase(Locale.ROOT) == "home") return@Comparator -1
                                if (o2.toLowerCase(Locale.ROOT) == "home") return@Comparator 1
                                return@Comparator o1.compareTo(o2, ignoreCase = true)
                            }
                    )
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    viewModel.getTopStories(getApiSection(adapter, position))
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No-op
                }
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = newsReaderAdapter
        }

        with(binding.refresh) {
            setOnRefreshListener {
                with(binding.spinner) {
                    viewModel.getTopStories(getApiSection(adapter, selectedItemPosition), true)
                }
            }
        }
    }

    private fun setupLiveData() {
        viewModel.newsReaderState.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is NewsReaderState.Loading -> {
                    Log.d(TAG, "--- origin: ${viewState.origin}, loading")
                    RealmStateHelper.loading(binding)
                }
                is NewsReaderState.Data -> {
                    Log.d(TAG, "--- origin: ${viewState.origin}, elements: ${viewState.data.size}")
                    RealmStateHelper.data(binding, viewState.data, newsReaderAdapter)
                }
                is NewsReaderState.NoNewData -> {
                    Log.d(TAG, "--- origin: ${viewState.origin}, no new data")
                    RealmStateHelper.noNewData(binding)
                }
                is NewsReaderState.ErrorException -> {
                    val stacktrace = viewState.throwable.cause?.stackTrace?.joinToString { "$it\n" }
                    Log.e(TAG, "--- error (exception): ${viewState.throwable.message} - ${viewState.throwable.cause?.message}: $stacktrace")
                    RealmStateHelper.error(binding)
                }
                is NewsReaderState.ErrorMessage -> {
                    Log.e(TAG, "--- error (message): ${viewState.message}")
                    RealmStateHelper.error(binding)
                }
            }
        })
    }

    private fun getApiSection(adapter: SpinnerAdapter, position: Int): String {
        val apiSection = adapter.getItem(position) as String
        return requireNotNull(sectionsToNames[apiSection])
    }

    companion object {

        const val TAG = "MainFragment"

        fun newInstance() = MainFragment()
    }
}

sealed class NewsReaderState {

    abstract val origin: String

    data class Loading(override val origin: String) : NewsReaderState()
    data class Data(override val origin: String, val data: List<RealmNYTimesArticle>) : NewsReaderState()
    data class NoNewData(override val origin: String) : NewsReaderState()
    data class ErrorException(override val origin: String, val throwable: Throwable) : NewsReaderState()
    data class ErrorMessage(override val origin: String, val message: String) : NewsReaderState()
}

private object RealmStateHelper {
    fun loading(binding: FragmentMainBinding) {
        if (!binding.refresh.isRefreshing) {
            binding.refresh.setRefreshing(true)
        }
    }

    fun data(
            binding: FragmentMainBinding,
            data: List<RealmNYTimesArticle>,
            newsReaderAdapter: MainAdapter
    ) {
        hideLoadingSpinner(binding)
        newsReaderAdapter.submitList(data)
    }

    fun noNewData(binding: FragmentMainBinding) {
        hideLoadingSpinner(binding)
    }

    fun error(binding: FragmentMainBinding) {
        hideLoadingSpinner(binding)
        Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
    }

    private fun hideLoadingSpinner(binding: FragmentMainBinding) {
        if (binding.refresh.isRefreshing) {
            binding.refresh.setRefreshing(false)
        }
    }

    private fun showLoadingSpinner(binding: FragmentMainBinding) {
        if (!binding.refresh.isRefreshing) {
            binding.refresh.setRefreshing(true)
        }
    }
}
