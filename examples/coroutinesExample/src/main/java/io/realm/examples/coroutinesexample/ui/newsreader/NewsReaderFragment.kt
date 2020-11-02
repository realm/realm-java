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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.databinding.FragmentNewsReaderBinding
import io.realm.examples.coroutinesexample.ui.dog.DomainDogAdapter

class NewsReaderFragment : Fragment() {

    private val viewModel: NewsReaderViewModel by viewModels()
//    private val articleAdapter = DomainArticleAdapter()

    private lateinit var binding: FragmentNewsReaderBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = FragmentNewsReaderBinding.inflate(inflater, container, false)
            .also { binding ->
                this.binding = binding
                viewModel.refreshTopStories()
            }.root

    private fun setupRecyclerView() {
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
//            adapter = dogAdapter
        }

        with(binding.refresh) {
            setOnRefreshListener {
//                viewModel.refreshDogs()
            }
        }
    }

    companion object {
        fun newInstance() = NewsReaderFragment()
    }
}
