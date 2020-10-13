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

package com.mongodb.realm.example

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.realm.*
import com.mongodb.realm.example.model.CRDTCounter
import com.mongodb.realm.example.databinding.ActivityCounterBinding
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.RealmLog
import io.realm.mongodb.User
import io.realm.mongodb.sync.ProgressListener
import io.realm.mongodb.sync.ProgressMode
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
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

    private var realm: Realm? = null
    private lateinit var session: SyncSession
    private var user: User? = null

    private lateinit var counterView: TextView
    private lateinit var progressBar: MaterialProgressBar
    private lateinit var counter: CRDTCounter // Keep strong reference to counter to keep change listeners alive.

    private val loggedInUser: User?
        get() {
            var user: User? = null

            try {
                user = APP.currentUser()
            } catch (e: IllegalStateException) {
                RealmLog.warn(e);
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
        val user = user
        if (user != null) {
            // Create a RealmConfiguration for our user
            // Use user id as partition value, so each user gets an unique view.
            // FIXME Right now we are using waitForInitialRemoteData and a more advanced
            // initialData block due to Sync only supporting ObjectId keys. This should
            // be changed once natural keys are supported.
            val config = SyncConfiguration.Builder(user, user.id)
                    .initialData {
                        if (it.isEmpty) {
                            it.insert(CRDTCounter())
                        }
                    }
                    .waitForInitialRemoteData()
                    .build()

            // This will automatically sync all changes in the background for as long as the Realm is open
            Realm.getInstanceAsync(config, object: Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    this@CounterActivity.realm = realm

                    counter = realm.where<CRDTCounter>().findFirstAsync()
                    counter.addChangeListener<CRDTCounter> { obj, _ ->
                        if (obj.isValid) {
                            counterView.text = String.format(Locale.US, "%d", counter.count)
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
            })
            counterView.text = "-"
        }
    }

    override fun onStop() {
        super.onStop()
        user?.run {
            session.run {
                removeProgressListener(downloadListener)
                removeProgressListener(uploadListener)
            }
            realm?.close()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_counter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val user = user
                user?.logOutAsync {
                    if (it.isSuccess) {
                        realm?.close()
                        this.user = loggedInUser
                    } else {
                        RealmLog.error(it.error.toString())
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateProgressBar(downloading: Boolean, uploading: Boolean) {
        val color = when {
            downloading && uploading -> R.color.progress_both
            downloading -> R.color.progress_download
            uploading -> R.color.progress_upload
            else -> android.R.color.black
        }
        progressBar.indeterminateDrawable.setColorFilter(resources.getColor(color), PorterDuff.Mode.SRC_IN)
        progressBar.visibility = if (color == android.R.color.black) View.GONE else View.VISIBLE
    }

    private fun adjustCounter(adjustment: Int) {
        // A synchronized Realm can get written to at any point in time, so doing synchronous writes on the UI
        // thread is HIGHLY discouraged as it might block longer than intended. Use only async transactions.
        realm?.executeTransactionAsync { realm ->
            val counter = realm.where<CRDTCounter>().findFirst()
            counter?.incrementCounter(adjustment.toLong())
        }
    }

}
