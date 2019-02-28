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

package io.realm.examples.objectserver.advanced.ui.checkin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.realm.examples.objectserver.advanced.R
import io.realm.examples.objectserver.advanced.databinding.ActivityCheckinBinding
import io.realm.examples.objectserver.advanced.model.entities.ActivityId
import io.realm.examples.objectserver.advanced.ui.BaseActivity
import io.realm.examples.objectserver.advanced.ui.bookingslist.BookingsListActivity


class CheckinActivity : BaseActivity() {

    companion object {
        const val INTENT_EXTRA_ACTIVIY_ID = "CheckinActivity.activityId"
    }

    private lateinit var viewModel: CheckinViewModel
    private lateinit var binding: ActivityCheckinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedExcursion = ActivityId(intent.getStringExtra(INTENT_EXTRA_ACTIVIY_ID))
        viewModel = ViewModelProviders.of(
                this,
                viewModelFactory { CheckinViewModel(selectedExcursion) }
        ).get(CheckinViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_checkin)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Setup UI
        setSupportActionBar(findViewById(R.id.toolbar))
        viewModel.title().observe(this, Observer {
            binding.toolbar.title = it
        })

        val timeslotAdapter = TimeslotAdapter(this, viewModel.timeslots())
        binding.timeslots.adapter = timeslotAdapter
        binding.timeslots.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectOffering(null)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectOffering(timeslotAdapter.getItem(position)!!)
            }
        }

        // Setup navigation
        viewModel.navigate().observe(this, Observer { target ->
            when(target.first) {
                CheckinViewModel.NavigationTarget.CheckedInGuests -> {
                    val intent = Intent(this, BookingsListActivity::class.java).apply {
                        putExtra(BookingsListActivity.INTENT_EXTRA_OFFERING_ID, target.second.value)
                        action = BookingsListActivity.INTENT_ACTION_CANCEL_CHECKIN
                    }
                    startActivity(intent)
                }
                CheckinViewModel.NavigationTarget.RemainingGuests -> {
                    val intent = Intent(this, BookingsListActivity::class.java).apply {
                        putExtra(BookingsListActivity.INTENT_EXTRA_OFFERING_ID, target.second.value)
                        action = BookingsListActivity.INTENT_ACTION_CHECKIN
                    }
                    startActivity(intent)
                }
                CheckinViewModel.NavigationTarget.AdhocGuest -> {
                    Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Select default starting time
        if (!timeslotAdapter.isEmpty) {
            viewModel.selectOffering(timeslotAdapter.getItem(0)!!)
        }
    }
}
