/*
 * Copyright 2019 Realm Inc.
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

package io.realm.examples.objectserver.activitytracker.ui.orderlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.objectserver.activitytracker.databinding.ItemOrderListBinding
import io.realm.examples.objectserver.activitytracker.model.entities.Order


class OrdersRecyclerAdapter(private val viewModel: OrdersViewModel): RecyclerView.Adapter<OrdersRecyclerAdapter.MyViewHolder>() {

    private var data: List<Order> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemOrderListBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<Order>) {
        this.data = data
    }

    // See https://medium.com/androiddevelopers/android-data-binding-recyclerview-db7c40d9f0e4
    inner class MyViewHolder(private val binding: ItemOrderListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Order) {
            binding.item = item
            binding.vm = viewModel
            binding.executePendingBindings()
        }
    }
}
