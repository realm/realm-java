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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.databinding.MainFragmentBinding
import io.realm.examples.coroutinesexample.model.Dog

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return MainFragmentBinding.inflate(inflater)
                .also { addClickListeners(it) }
                .root
    }

    override fun onResume() {
        super.onResume()
        addObservers()
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    private fun addClickListeners(binding: MainFragmentBinding) {
        binding.buttonHeavyTransaction.setOnClickListener {
            // Calling this multiple times before each transaction is done will not
            // freeze the UI
            viewModel.insertDogs(100000)
        }
        binding.buttonTransaction.setOnClickListener {
            // Calling this multiple times before each transaction is done will not
            // freeze the UI
            viewModel.insertDogs()
        }
        binding.buttonCount.setOnClickListener {
            viewModel.countDogs()
        }
        binding.buttonDelete.setOnClickListener {
            // Calling this while bulk-inserting will not freeze the UI
            viewModel.deleteAll()
        }
        binding.buttonCancelCoroutine.setOnClickListener {
            viewModel.cancel()
        }
    }

    private fun addObservers() {
        viewModel.getDogs().observe(viewLifecycleOwner, dogsObserver)
        viewModel.count.observe(viewLifecycleOwner, countObserver)
    }

    private fun removeObservers() {
        viewModel.getDogs().removeObserver(dogsObserver)
        viewModel.count.removeObserver(countObserver)
    }

    private val dogsObserver = Observer<List<Dog>> { doggos ->
        Log.d(TAG, "Added ${doggos.size}")
    }

    private val countObserver = Observer<Long> { count ->
        Log.d(TAG, "count: $count")
    }
}
