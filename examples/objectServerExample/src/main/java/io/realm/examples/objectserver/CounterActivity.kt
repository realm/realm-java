/*
 * Copyright 2016 Realm Inc.
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

package io.realm.examples.objectserver

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import io.realm.*
import io.realm.examples.objectserver.databinding.ActivityCounterBinding
import io.realm.examples.objectserver.model.CRDTCounter
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class CounterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounterBinding

    private val downloadListener = ProgressListener { progress ->
        downloadingChanges.set(!progress.isTransferComplete)
        runOnUiThread(updateProgressBar)
    }
    private val uploadListener = ProgressListener { progress ->
        uploadingChanges.set(!progress.isTransferComplete)
        runOnUiThread(updateProgressBar)
    }
    private val updateProgressBar = Runnable { updateProgressBar(downloadingChanges.get(), uploadingChanges.get()) }

    private val downloadingChanges = AtomicBoolean(false)
    private val uploadingChanges = AtomicBoolean(false)

    private lateinit var realm: Realm
    private lateinit var session: SyncSession
    private var user: SyncUser? = null

    private lateinit var counterView: TextView
    private lateinit var progressBar: MaterialProgressBar
    private lateinit var counters: RealmResults<CRDTCounter> // Keep strong reference to counter to keep change listeners alive.

    private val loggedInUser: SyncUser?
        get() {
            var user: SyncUser? = null

            try {
                user = SyncUser.current()
            } catch (ignore: IllegalStateException) {
            }

            if (user == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            return user
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_counter)
        counterView = binding.textCounter
        progressBar = binding.progressbar
        binding.upper.setOnClickListener { adjustCounter(1) }
        binding.lower.setOnClickListener { adjustCounter(-1) }
    }

    override fun onStart() {
        super.onStart()
        user = loggedInUser
        user?.let {

            // Create a RealmConfiguration for our user
            val config = user!!.createConfiguration(BuildConfig.REALM_URL)
                    .initialData { realm -> realm.createObject(CRDTCounter::class.java, it.identity) }
                    .build()

            // This will automatically sync all changes in the background for as long as the Realm is open
            realm = Realm.getInstance(config)

            counterView.text = "-"
            counters = realm.where<CRDTCounter>().equalTo("name", it.identity).findAllAsync()
            counters.addChangeListener { counters, changeSet ->
                if (counters.isValid && !counters.isEmpty()) {
                    val counter = counters.first()
                    counterView.text = String.format(Locale.US, "%d", counter!!.count)
                } else {
                    counterView.text = "-"
                }
            }

            // Setup progress listeners for indeterminate progress bars
            session = realm.syncSession
            session.run {
                addDownloadProgressListener(ProgressMode.INDEFINITELY, downloadListener)
                addUploadProgressListener(ProgressMode.INDEFINITELY, uploadListener)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        user?.let {
            session.run {
                removeProgressListener(downloadListener)
                removeProgressListener(uploadListener)
            }
            realm.close()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_counter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                realm.close()
                user?.let  {
                    it.logOut()
                    this.user = loggedInUser
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateProgressBar(downloading: Boolean, uploading: Boolean) {
        @ColorRes var color = android.R.color.black
        var visibility = View.VISIBLE
        if (downloading && uploading) {
            color = R.color.progress_both
        } else if (downloading) {
            color = R.color.progress_download
        } else if (uploading) {
            color = R.color.progress_upload
        } else {
            visibility = View.GONE
        }
        progressBar.indeterminateDrawable.setColorFilter(resources.getColor(color), PorterDuff.Mode.SRC_IN)
        progressBar.visibility = visibility
    }

    private fun adjustCounter(adjustment: Int) {
        // A synchronized Realm can get written to at any point in time, so doing synchronous writes on the UI
        // thread is HIGHLY discouraged as it might block longer than intended. Use only async transactions.
        realm.executeTransactionAsync { realm ->
            val counter = realm.where(CRDTCounter::class.java).findFirst()
            counter?.incrementCounter(adjustment.toLong())
        }
    }

}
