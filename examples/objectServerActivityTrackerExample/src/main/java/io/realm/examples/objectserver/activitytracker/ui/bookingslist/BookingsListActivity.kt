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

package io.realm.examples.objectserver.advanced.ui.bookingslist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.objectserver.advanced.R
import io.realm.examples.objectserver.advanced.databinding.ActivityBookingsListBinding
import io.realm.examples.objectserver.advanced.model.entities.TimeSlotId
import io.realm.examples.objectserver.advanced.ui.BaseActivity
import java.util.*


class BookingsListActivity : BaseActivity() {

    companion object {
        const val INTENT_EXTRA_OFFERING_ID = "BookingsListActivity.TimeSlotId"
        const val INTENT_ACTION_CHECKIN = "BookingsListActivity.Checkin"
        const val INTENT_ACTION_CANCEL_CHECKIN = "BookingsListActivity.CancelCheckin"
    }

    private lateinit var viewModel: BookingsListViewModel
    private lateinit var binding: ActivityBookingsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = TimeSlotId(intent.getStringExtra(INTENT_EXTRA_OFFERING_ID))
        viewModel = when (intent.action) {
            INTENT_ACTION_CHECKIN -> {
                ViewModelProviders.of(
                        this,
                        viewModelFactory { BookingsListViewModel(id, BookingsListViewModel.Mode.CheckIn) }
                ).get(BookingsListViewModel::class.java)
            }
            INTENT_ACTION_CANCEL_CHECKIN -> {
                ViewModelProviders.of(
                        this,
                        viewModelFactory { BookingsListViewModel(id, BookingsListViewModel.Mode.CancelCheckIn) }
                ).get(BookingsListViewModel::class.java)
            }
            else -> throw IllegalArgumentException("Unsupported action: ${intent.action}")
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_bookings_list)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Setup UI
        setSupportActionBar(binding.toolbar)
        viewModel.title().observe(this, Observer {
            binding.toolbar.title = it
        })

        binding.textSearch.addTextChangedListener(object: TextWatcher {
            private var timer = Timer()
            private val DELAY: Long = 200 // milliseconds
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            viewModel.setSearchCriteria(binding.textSearch.text.toString())
                        }
                    }
                }, DELAY)
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel.bookings().observe(this, Observer {
            // If the query is updated we need to replace the query result completely instead
            // of getting fine-grained animations. This is a bit sub-optimal, but the best we
            // can do so far. See https://github.com/realm/realm-java/issues/6216
            val bookingsAdapter = BookingsRecyclerAdapter(viewModel, it)
            recyclerView.adapter = bookingsAdapter
        })

        // Setup navigation
        viewModel.navigate().observe(this, Observer {
            when(it.first) {
                // For now, just assume we always return to the Checkin overview screen
                BookingsListViewModel.NavigationTarget.CheckinOverview -> finish()
            }
        })
    }
}
