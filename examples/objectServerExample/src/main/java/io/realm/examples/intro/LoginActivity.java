package io.realm.examples.intro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.objectserver.Credentials;
import io.realm.objectserver.SyncManager;
import io.realm.objectserver.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

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
            onLoginFailed();
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

        User.authenticate(
                Credentials.fromUsernamePassword(username, password),
                "http://127.0.0.1:8080/auth",
                true,
                new User.Callback() {
                @Override
                public void onSuccess(User user) {
                    progressDialog.dismiss();
//                    UserUtils.setCurrentUser(user);
                    onLoginSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    progressDialog.dismiss();
                    onLoginFailed();
                }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
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

    public void onLoginFailed() {
        loginButton.setEnabled(true);
        createUserButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;
        String email = username.getText().toString();
        String password = this.password.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            username.setError("Enter a valid email address");
            valid = false;
        } else {
            username.setError(null);
        }

        if (password.isEmpty()) {
            this.password.setError("Non-empty password required.");
            valid = false;
        } else {
            this.password.setError(null);
        }

        return valid;
    }
}
