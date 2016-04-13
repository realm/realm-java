package realm.io.storeencryptionpassword;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
            Arrays.fill(realmKey, (byte) 0);
        }

        Toast.makeText(MainActivity.this, "Unlocking ...", Toast.LENGTH_SHORT).show();
        mBtnUnlock.setEnabled(false);

        final byte[] realmKey = store.decryptKeyForRealm(encryptedRealmKey);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).encryptionKey(realmKey).build();
        Realm.setDefaultConfiguration(realmConfig);

        goToList();
    }

    private void goToList () {

        mBtnLock.setEnabled(true);

        //start Todo list
        Intent intent = new Intent(MainActivity.this, SecretTodoList.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
