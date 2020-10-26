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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.realm.examples.coroutinesexample.databinding.MainFragmentBinding

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
        return MainFragmentBinding.inflate(inflater, container, false)
                .also {
                    addClickListeners(it)
                    it.viewModel = viewModel
                    it.lifecycleOwner = viewLifecycleOwner
                }
                .root
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
        binding.buttonDelete.setOnClickListener {
            // Calling this while bulk-inserting will not freeze the UI
            viewModel.deleteAll()
        }
        binding.buttonCancelCoroutine.setOnClickListener {
            viewModel.cancel()
        }
    }
}
