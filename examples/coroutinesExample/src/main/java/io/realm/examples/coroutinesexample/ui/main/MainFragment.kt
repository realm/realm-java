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
import com.dropbox.android.external.store4.ExperimentalStoreApi
import io.realm.examples.coroutinesexample.databinding.MainFragmentBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@ExperimentalStoreApi
@FlowPreview
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
                }.root
    }

    private fun addClickListeners(binding: MainFragmentBinding) {
        binding.buttonGet.setOnClickListener {
            viewModel.getDogs()
        }

        binding.buttonRefresh.setOnClickListener {
            viewModel.refreshDogs()
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.deleteDogs()
        }
    }
}
