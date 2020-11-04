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

package io.realm.examples.coroutinesexample.ui.newsreader.room

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
import io.realm.examples.coroutinesexample.data.newsreader.local.room.RoomNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.network.sectionsToNames
import io.realm.examples.coroutinesexample.databinding.FragmentNewsReaderBinding
import java.util.*

/**
 * Room implementation.
 */
class RoomNewsReaderFragment : Fragment() {

    private val viewModel: RoomNewsReaderViewModel by viewModels()
    private val newsReaderAdapter = RoomNewsReaderAdapter()

    private lateinit var binding: FragmentNewsReaderBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = FragmentNewsReaderBinding.inflate(inflater, container, false)
            .also { binding ->
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
                    sectionsToNames.values.toTypedArray()
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    viewModel.getTopStories(getKey(adapter, position))
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
                    val key = getKey(adapter, selectedItemPosition)
                    viewModel.getTopStories(key, true)
                }
            }
        }
    }

    private fun setupLiveData() {
        viewModel.newsReaderState.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is RoomNewsReaderState.Loading -> RoomStateHelper.loading(binding)
                is RoomNewsReaderState.Data -> RoomStateHelper.data(binding, viewState.data, newsReaderAdapter)
                is RoomNewsReaderState.NoNewData -> RoomStateHelper.noNewData(binding)
                is RoomNewsReaderState.ErrorException -> RoomStateHelper.errorException(binding, viewState.throwable)
                is RoomNewsReaderState.ErrorMessage -> RoomStateHelper.errorMessage(binding, viewState.message)
            }
        })
    }

    private fun getKey(adapter: SpinnerAdapter, position: Int): String {
        return sectionsToNames.let { sectionMap ->
            for (key in sectionMap.keys) {
                if (key.toLowerCase(Locale.ROOT) == (adapter.getItem(position) as String).toLowerCase(Locale.ROOT)) {
                    return@let key
                }
            }
            throw IllegalStateException("Key not found")
        }
    }

    companion object {
        fun newInstance() = RoomNewsReaderFragment()
    }
}

sealed class RoomNewsReaderState {

    abstract val origin: String

    data class Loading(override val origin: String) : RoomNewsReaderState()
    data class Data(override val origin: String, val data: List<RoomNYTimesArticle>) : RoomNewsReaderState()
    data class NoNewData(override val origin: String) : RoomNewsReaderState()
    data class ErrorException(override val origin: String, val throwable: Throwable) : RoomNewsReaderState()
    data class ErrorMessage(override val origin: String, val message: String) : RoomNewsReaderState()
}

private object RoomStateHelper {
    fun loading(binding: FragmentNewsReaderBinding) {
        if (!binding.refresh.isRefreshing) {
            binding.refresh.setRefreshing(true)
        }
    }

    fun data(
            binding: FragmentNewsReaderBinding,
            data: List<RoomNYTimesArticle>,
            newsReaderAdapter: RoomNewsReaderAdapter
    ) {
        hideLoadingSpinner(binding)
        newsReaderAdapter.submitList(data)
    }

    fun noNewData(binding: FragmentNewsReaderBinding) {
        hideLoadingSpinner(binding)
    }

    fun errorException(binding: FragmentNewsReaderBinding, throwable: Throwable) {
        hideLoadingSpinner(binding)
        val stacktrace = throwable.cause?.stackTrace?.joinToString { "$it\n" }
        Log.e(TAG, "--- error (exception): ${throwable.message} - ${throwable.cause?.message}: $stacktrace")
        Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
    }

    fun errorMessage(binding: FragmentNewsReaderBinding, message: String) {
        hideLoadingSpinner(binding)
        Log.e(TAG, "--- error (message): $message")
        Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
    }

    private fun hideLoadingSpinner(binding: FragmentNewsReaderBinding) {
        if (binding.refresh.isRefreshing) {
            binding.refresh.setRefreshing(false)
        }
    }
}
