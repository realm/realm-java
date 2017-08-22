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

package io.realm.examples.securetokenandroidkeystore;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.securetokenandroidkeystore.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStoreException;
import java.util.UUID;

import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.android.SecureUserStore;
import io.realm.internal.objectserver.Token;

/**
 * Activity responsible of unlocking the KeyStore
 * before using the {@link io.realm.android.SecureUserStore} to encrypt
 * the Token we get from the session
 */
public class MainActivity extends AppCompatActivity {
    private TextView txtKeystoreState;

    private SecureUserStore secureUserStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtKeystoreState = (TextView) findViewById(R.id.txtLabelKeyStore);

        try {
            secureUserStore = new SecureUserStore(this);
            SyncManager.setUserStore(secureUserStore);

            if (secureUserStore.isKeystoreUnlocked()) {
                buildSyncConf();
                keystoreUnlockedMessage();
            } else {
                secureUserStore.unlockKeystore();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // We return to the app after the KeyStore is unlocked or not.
            if (secureUserStore.isKeystoreUnlocked()) {
                buildSyncConf();
                keystoreUnlockedMessage();
            } else {
                keystoreLockedMessage();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    // build SyncConfiguration with a user store to store encrypted Token.
    private void buildSyncConf() {
        // the rest of Sync logic ...
        SyncUser user = createTestUser(Long.MAX_VALUE);
        String url = "realm://objectserver.realm.io/default";
        SyncConfiguration secureConfig = new SyncConfiguration.Builder(user, url).build();
        Realm realm = Realm.getInstance(secureConfig);
        // ...
    }

    // Helpers
    private final static String USER_TOKEN = UUID.randomUUID().toString();

    private static SyncUser createTestUser(long expires) {
        Token userToken = new Token(USER_TOKEN, "JohnDoe", null, expires, null);
        JSONObject obj = new JSONObject();
        try {
            JSONObject realmDesc = new JSONObject();
            realmDesc.put("uri", "realm://objectserver.realm.io/default");

            obj.put("authUrl", "http://objectserver.realm.io/auth");
            obj.put("userToken", userToken.toJson());
            return SyncUser.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void keystoreLockedMessage() {
        txtKeystoreState.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLocked));
        txtKeystoreState.setText(R.string.locked_text);
    }

    private void keystoreUnlockedMessage() {
        txtKeystoreState.setBackgroundColor(ContextCompat.getColor(this, R.color.colorActivated));
        txtKeystoreState.setText(R.string.unlocked_text);
    }
}

