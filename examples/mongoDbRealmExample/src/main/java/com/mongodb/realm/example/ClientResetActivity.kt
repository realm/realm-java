/*
 * Copyright 2021 Realm Inc.
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

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mongodb.realm.example.databinding.ActivityClientresetBinding
import io.realm.*
import io.realm.mongodb.sync.*
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * This class is used as an example on how to implement Client Reset.
 *
 * This activity is launched with `singleInstance` so no matter how many times it is
 * started only one instance is running. However, when finished, it will just return to whatever is
 * on the top of navigation stack.
 *
 * Pressing back has been disabled to prevent the [clientResetHelper] from accidentally running
 * while another Activity is displayed.
 */
class ClientResetActivity : AppCompatActivity() {

    companion object {
        // Track Client Reset errors as a queue of errors as the chance of all Realms
        // connected to a single app instance experiencing Client Resets is quite high, e.g.
        // in the case where Sync was terminated then restarted on the server.
        var RESET_ERRORS = ConcurrentLinkedQueue<Pair<ClientResetRequiredError, SyncConfiguration>>()
    }

    // Run Client Reset logic on a separate helper thread to make it easier to implement timeouts.
    // Note, this Runnable is not particular safe as it will continue running even if the Activity
    // is excited
    private val clientResetHelper = Runnable {
        var errorReported = false;
        ClientResetLoop@ while(true) {
            val error: Pair<ClientResetRequiredError, SyncConfiguration> = RESET_ERRORS.poll() ?: break
            val clientReset: ClientResetRequiredError = error.first
            val config: SyncConfiguration = error.second

            // The background Sync Client take about 10 seconds to fully close the connection and thus
            // the background Realm. Set timeout to 20 seconds.
            var maxWait = 20
            while (Realm.getGlobalInstanceCount(config) > 0) {
                if (maxWait == 0) {
                    runOnUiThread {
                        progressBar.visibility = View.INVISIBLE
                        statusView.text = "'${config.realmFileName}' did not fully close, so database could not be reset. Aborting"
                    }
                    errorReported = true
                    break@ClientResetLoop
                } else {
                    maxWait--
                    runOnUiThread {
                        statusView.text = "Waiting for '${config.realmFileName}' to fully close ($maxWait): ${Realm.getGlobalInstanceCount(config)}"
                    }
                    SystemClock.sleep(1000)
                }
            }
            clientReset.executeClientReset()
            runOnUiThread {
                statusView.text = ""
            }
        }
        if (!errorReported) {
            finish()
        }
    }

    private lateinit var binding: ActivityClientresetBinding

    private lateinit var statusView: TextView
    private lateinit var progressBar: MaterialProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_clientreset)
        statusView = binding.status
        progressBar = binding.progressbar
    }

    override fun onResume() {
        super.onResume()
        Thread(clientResetHelper).start();
    }

    override fun onBackPressed() {
        Toast.makeText(
                this,
                "Pressing 'Back' is disabled while Client Reset is running.",
                Toast.LENGTH_LONG
        ).show()
    }
}
