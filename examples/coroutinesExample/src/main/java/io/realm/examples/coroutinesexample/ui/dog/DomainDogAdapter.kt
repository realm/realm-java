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

package io.realm.examples.coroutinesexample.ui.dog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.domain.dog.model.DomainDog

class DomainDogAdapter : ListAdapter<DomainDog, DomainDogAdapter.DogViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dog, parent, false)
                    .let { view -> DogViewHolder(view) }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = getItem(position)
        holder.dogName.text = dog.name
        holder.dogAge.text = dog.age.toString()
    }

    inner class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dogName: TextView = view.findViewById(R.id.dogName)
        val dogAge: TextView = view.findViewById(R.id.dogAge)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DomainDog>() {
            override fun areItemsTheSame(oldItem: DomainDog, newItem: DomainDog): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: DomainDog, newItem: DomainDog): Boolean =
                    oldItem.name == newItem.name && oldItem.age == newItem.age
        }
    }
}
