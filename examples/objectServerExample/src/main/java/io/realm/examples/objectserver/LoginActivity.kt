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

package io.realm.examples.objectserver

import android.app.ProgressDialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.realm.ErrorCode
import io.realm.ObjectServerError
import io.realm.SyncCredentials
import io.realm.SyncUser
import io.realm.examples.objectserver.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var createUserButton: Button

    lateinit private var binding: ActivityLoginBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        username = binding.inputUsername
        password = binding.inputPassword
        loginButton = binding.buttonLogin
        createUserButton = binding.buttonCreate

        loginButton.setOnClickListener { login(false) }
        createUserButton.setOnClickListener { login(true) }
    }

    private fun login(createUser: Boolean) {
        if (!validate()) {
            onLoginFailed("Invalid username or password")
            return
        }

        binding.buttonCreate.isEnabled = false
        binding.buttonLogin.isEnabled = false

        val progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating...")
        progressDialog.show()

        val username = this.username.text.toString()
        val password = this.password.text.toString()

        val creds = SyncCredentials.usernamePassword(username, password, createUser)
        val callback = object : SyncUser.Callback<SyncUser> {
            override fun onSuccess(user: SyncUser) {
                progressDialog.dismiss()
                onLoginSuccess()
            }

            override fun onError(error: ObjectServerError) {
                progressDialog.dismiss()
                val errorMsg: String = when (error.errorCode) {
                    ErrorCode.UNKNOWN_ACCOUNT -> getString(R.string.login_error_unknown_account)
                    ErrorCode.INVALID_CREDENTIALS -> getString(R.string.login_error_invalid_credentials)
                    else -> error.toString()
                }
                onLoginFailed(errorMsg)
            }
        }

        SyncUser.logInAsync(creds, BuildConfig.REALM_AUTH_URL, callback)
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() {
        loginButton.isEnabled = true
        createUserButton.isEnabled = true
        finish()
    }

    private fun onLoginFailed(errorMsg: String) {
        loginButton.isEnabled = true
        createUserButton.isEnabled = true
        Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun validate(): Boolean = when {
        username.text.toString().isEmpty() -> false
        password.text.toString().isEmpty() -> false
        else -> true
    }
}
