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

package io.realm.examples.objectserver.advanced.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.DataBindingUtil
import io.realm.examples.objectserver.advanced.Constants
import io.realm.examples.objectserver.advanced.R
import io.realm.examples.objectserver.advanced.databinding.ActivityLoginBinding
import io.realm.examples.objectserver.advanced.model.App
import io.realm.examples.objectserver.advanced.ui.activitylist.SelectActivityActivity
import io.realm.ErrorCode
import io.realm.ObjectServerError
import io.realm.SyncCredentials
import io.realm.SyncUser

class LoginActivity: AppCompatActivity() {

    private lateinit var username: AppCompatEditText
    private lateinit var password: AppCompatEditText
    private lateinit var loginButton: AppCompatButton
    private lateinit var createUserButton: AppCompatButton

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
                onLoginSuccess(user)
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

        SyncUser.logInAsync(creds, Constants.AUTH_URL, callback)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun onLoginSuccess(user: SyncUser) {
        loginButton.isEnabled = true
        createUserButton.isEnabled = true
        App.configureRealms(user)
        val intent = Intent(this, SelectActivityActivity::class.java)
        startActivity(intent)
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
