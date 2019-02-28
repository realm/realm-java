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

package io.realm.examples.objectserver.advanced.ui.activitylist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.objectserver.advanced.R
import io.realm.examples.objectserver.advanced.databinding.ActivityExcursionListBinding
import io.realm.examples.objectserver.advanced.ui.BaseActivity
import io.realm.examples.objectserver.advanced.ui.checkin.CheckinActivity
import io.realm.examples.objectserver.advanced.ui.orderlist.OrdersActivity

class SelectActivityActivity : BaseActivity() {

    private lateinit var viewModel: SelectActivityViewModel
    private lateinit var binding: ActivityExcursionListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_excursion_list)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Setup UI
        setSupportActionBar(findViewById(R.id.toolbar))
        val itemsRecyclerAdapter = ActivityRecyclerAdapter(viewModel, viewModel.excursions())
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = itemsRecyclerAdapter

        // Setup navigation
        viewModel.navigateTo.observe(this, Observer {
            when(it.first) {
                SelectActivityViewModel.NavigationTarget.ExcursionDetails -> {
                    val intent = Intent(this, CheckinActivity::class.java).apply {
                        putExtra(CheckinActivity.INTENT_EXTRA_ACTIVIY_ID, it.second.value)
                    }
                    startActivity(intent)
                }
                SelectActivityViewModel.NavigationTarget.Orders -> {
                    val intent = Intent(this, OrdersActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.cleanupSubscriptions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.apply {
            findItem(R.id.action_create_demo_data).isVisible = true
            findItem(R.id.action_goto_orders).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_create_demo_data -> {
                viewModel.createDemoData()
                true
            }
            R.id.action_goto_orders -> {
                viewModel.gotoOrdersSelected()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
