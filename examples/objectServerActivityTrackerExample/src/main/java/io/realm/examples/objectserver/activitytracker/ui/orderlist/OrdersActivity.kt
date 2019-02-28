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

package io.realm.examples.objectserver.advanced.ui.orderlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.objectserver.advanced.ui.BaseActivity
import io.realm.examples.objectserver.advanced.R


class OrdersActivity : BaseActivity() {

    private lateinit var viewModel: OrdersViewModel
    private lateinit var binding: io.realm.examples.objectserver.advanced.databinding.ActivityOrderListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(OrdersViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_list)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Setup UI
        setSupportActionBar(findViewById(R.id.toolbar))
        val adapter = OrdersRecyclerAdapter(viewModel)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        viewModel.orders().observe(this, Observer {
            adapter.setData(it.first)
            val diffResults = it.second
            if (diffResults != null) {
                diffResults.dispatchUpdatesTo(adapter)
            } else {
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.apply {
            findItem(R.id.action_create_order).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_create_order -> {
                viewModel.createOrder()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
