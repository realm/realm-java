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

package io.realm.examples.coroutinesexample.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.realm.examples.coroutinesexample.databinding.FragmentDetailsBinding
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModels()

    private lateinit var binding: FragmentDetailsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return FragmentDetailsBinding.inflate(inflater, container, false)
                .also { binding ->
                    this.binding = binding
                    binding.lifecycleOwner = viewLifecycleOwner
                    binding.viewModel = viewModel
                    setupLiveData()
                }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val id = requireNotNull(requireArguments().getString(ARG_ID))
        viewModel.loadDetails(id)
    }

    private fun setupLiveData() {
        viewModel.read.observe(viewLifecycleOwner, Observer {
            setRead(true)
        })
    }

    private fun setRead(read: Boolean) {
        with(binding.read) {
            if (read) {
                animate().alpha(1.0f)
            } else {
                animate().alpha(0f)
            }
        }
    }

    data class ArgsBundle(val id: String)

    companion object {

        const val TAG = "DetailsFragment"

        private const val ARG_ID = "id"

        fun instantiate(argsBundle: ArgsBundle): DetailsFragment {
            return DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, argsBundle.id)
                }
            }
        }
    }
}
