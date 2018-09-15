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

package io.realm.examples.objectserver;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.SyncCredentials;
import io.realm.ObjectServerError;
import io.realm.SyncUser;


public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.input_username) EditText username;
    @BindView(R.id.input_password) EditText password;
    @BindView(R.id.button_login) Button loginButton;
    @BindView(R.id.button_create) Button createUserButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(false);
            }
        });
        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(true);
            }
        });
    }

    public void login(boolean createUser) {
        if (!validate()) {
            onLoginFailed("Invalid username or password");
            return;
        }

        createUserButton.setEnabled(false);
        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        SyncCredentials creds = SyncCredentials.usernamePassword(username, password, createUser);
        SyncUser.Callback<SyncUser> callback = new SyncUser.Callback<SyncUser>() {
            @Override
            public void onSuccess(@Nonnull SyncUser user) {
                progressDialog.dismiss();
                onLoginSuccess();
            }

            @Override
            public void onError(@Nonnull ObjectServerError error) {
                progressDialog.dismiss();
                String errorMsg;
                switch (error.getErrorCode()) {
                    case UNKNOWN_ACCOUNT:
                        errorMsg = "Account does not exists.";
                        break;
                    case INVALID_CREDENTIALS:
                        errorMsg = "User name and password does not match";
                        break;
                    default:
                        errorMsg = error.toString();
                }
                onLoginFailed(errorMsg);
            }
        };

        SyncUser.logInAsync(creds, BuildConfig.REALM_AUTH_URL, callback);
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        createUserButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed(String errorMsg) {
        loginButton.setEnabled(true);
        createUserButton.setEnabled(true);
        Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;
        String email = username.getText().toString();
        String password = this.password.getText().toString();

        if (email.isEmpty()) {
            valid = false;
        }

        if (password.isEmpty()) {
            valid = false;
        }

        return valid;
    }
}
