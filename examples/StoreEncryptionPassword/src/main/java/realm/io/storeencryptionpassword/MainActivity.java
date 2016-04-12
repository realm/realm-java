package realm.io.storeencryptionpassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import io.realm.RealmConfiguration;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    private Button mBtnUnlock;
    private Button mBtnLock;
    private SharedPreferences defaultSharedPreferences;

    //TODO init Store (or get from Factory)
    // maybe a singelton since initialising encryption API could be expenses
    Store store = new Store() {
        @Override
        public boolean isKeystorePresent() {
            return false;
        }

        @Override
        public void generateKeystore() {

        }

        @Override
        public void unlockKeyStore() {

        }

        @Override
        public byte[] generateAesKey() {
            return new byte[0];
        }

        @Override
        public void encryptAndSaveAESKey(byte[] aes) {

        }

        @Override
        public byte[] decryptAesKey() {
            return new byte[0];
        }

        @Override
        public byte[] getRealmKey() {
            return new byte[0];
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBtnUnlock = (Button) findViewById(R.id.btnUnLock);
        mBtnLock = (Button) findViewById(R.id.btnLock);
        defaultSharedPreferences  = PreferenceManager.getDefaultSharedPreferences(this);

        byte[] encryptedRealmKey = store.getRealmKey();
        byte[] realmKey;
        if (encryptedRealmKey == null) {
            if (!store.isKeystorePresent()) {
                store.generateKeystore();
            }
            // generate an AES key
            realmKey = store.generateAesKey(); // key
            store.encryptAndSaveAESKey(realmKey);

        } else {
            // decrypt the key
            store.unlockKeyStore();
            realmKey = store.decryptAesKey();
        }

        //TODO use this one RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).encryptionKey(realmKey).build();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).build();
        Realm.setDefaultConfiguration(realmConfig);

        goToList();

        mBtnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Unlocking ...", Toast.LENGTH_SHORT).show();
                mBtnUnlock.setEnabled(false);
                // TODO try to unlock the keystore
                // Check if the AES key is already in the default SharedPref
                // if yes use it to create a RealmConfiguration otherwise
                // try to unlock the keystore to find one, of there is none
                // Generate one then encrypted using the private key stored
                // in the Keystore
                store.unlockKeyStore();
                byte[] realmKey = store.decryptAesKey();
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).build();
                Realm.setDefaultConfiguration(realmConfig);

                goToList();
            }
        });

        mBtnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnLock.setEnabled(false);
                Toast.makeText(MainActivity.this, "Locking ...", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                edit.remove("key");
                edit.apply();
                Realm.setDefaultConfiguration(null);
                mBtnUnlock.setEnabled(true);
            }
        });
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
