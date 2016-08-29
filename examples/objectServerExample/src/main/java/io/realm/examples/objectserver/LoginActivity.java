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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.objectserver.Credentials;
import io.realm.objectserver.Error;
import io.realm.objectserver.ObjectServerError;
import io.realm.objectserver.User;
import io.realm.objectserver.util.UserStore;

import static android.net.sip.SipErrorCode.INVALID_CREDENTIALS;
import static io.realm.objectserver.Error.UNKNOWN_ACCOUNT;

public class LoginActivity extends AppCompatActivity {

    private UserStore userStore = MyApplication.USER_STORE;

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

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        Credentials creds = Credentials.fromUsernamePassword(username, password, createUser);
        String authUrl = "http://192.168.1.3:8080/auth";
        User.Callback callback = new User.Callback() {
            @Override
            public void onSuccess(User user) {
                progressDialog.dismiss();
                userStore.saveAsync(MyApplication.APP_USER_KEY, user); // TODO Use Async
                userStore.setCurrentUser(user);
                onLoginSuccess();
            }

            @Override
            public void onError(ObjectServerError error) {
                progressDialog.dismiss();
                String errorMsg;
                switch (error.errorCode()) {
                    case UNKNOWN_ACCOUNT:
                        errorMsg = "Account does not exists.";
                        break;
                    case INVALID_CREDENTIALS:
                        errorMsg = "User name and password does not match";
                        break;
                    default:
                        errorMsg = "Unknown error. Try again";
                }
                onLoginFailed(errorMsg);
            }
        };

        User.login(creds, authUrl, callback);
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
            username.setError("Username required");
            valid = false;
        } else {
            username.setError(null);
        }

        if (password.isEmpty()) {
            this.password.setError("Password required");
            valid = false;
        } else {
            this.password.setError(null);
        }

        return valid;
    }
}
