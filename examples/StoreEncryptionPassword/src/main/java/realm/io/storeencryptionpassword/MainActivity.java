package realm.io.storeencryptionpassword;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    public static final int REQ_UNLOCK = 1;

    private Button mBtnUnlock;
    private Button mBtnLock;
    private View mBtnOpen;

    private final Store store = new Store(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBtnUnlock = (Button) findViewById(R.id.btnUnLock);
        mBtnLock = (Button) findViewById(R.id.btnLock);

        //TODO use this one RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).encryptionKey(realmKey).build();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).build();
        Realm.setDefaultConfiguration(realmConfig);

        mBtnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                store.unlockKeyStore(REQ_UNLOCK);
            }
        });

        mBtnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnLock.setEnabled(false);
                Toast.makeText(MainActivity.this, "Locking ...", Toast.LENGTH_SHORT).show();
                Realm.setDefaultConfiguration(new RealmConfiguration.Builder(MainActivity.this).build());
                mBtnUnlock.setEnabled(true);
            }
        });

        //noinspection ConstantConditions
        mBtnOpen = findViewById(R.id.btnOpenList);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToList();
            }
        });

        mBtnOpen.setEnabled(store.getEncryptedRealmKey() != null && store.containsEncryptionKey());

        mBtnUnlock.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_UNLOCK:
                if (store.onUnlockKeyStoreResult(resultCode, data)) {
                    onKeystoreUnlocked();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onKeystoreUnlocked() {
        byte[] encryptedRealmKey = store.getEncryptedRealmKey();
        if (encryptedRealmKey == null || !store.containsEncryptionKey()) {
            final byte[] realmKey = store.generateKeyForRealm();
            store.generateKeyInKeystore();
            encryptedRealmKey = store.encryptAndSaveKeyForRealm(realmKey);

            final RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this)
                    .encryptionKey(realmKey)
                    .build();
            Arrays.fill(realmKey, (byte) 0);

            // create encrypted Realm
            Realm.deleteRealm(realmConfig);
            Realm.getInstance(realmConfig).close();

            mBtnOpen.setEnabled(true);
        }

        Toast.makeText(MainActivity.this, "Unlocking ...", Toast.LENGTH_SHORT).show();
        mBtnUnlock.setEnabled(false);

        final byte[] realmKey = store.decryptKeyForRealm(encryptedRealmKey);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).encryptionKey(realmKey).build();
        Realm.setDefaultConfiguration(realmConfig);

        mBtnLock.setEnabled(true);
    }

    private void goToList () {
        //start Todo list
        Intent intent = new Intent(MainActivity.this, SecretTodoList.class);
        startActivity(intent);
    }
}
