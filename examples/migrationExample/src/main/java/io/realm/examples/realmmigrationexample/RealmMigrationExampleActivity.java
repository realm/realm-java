package io.realm.examples.realmmigrationexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.examples.realmmigrationexample.model.Migration;
import io.realm.examples.realmmigrationexample.model.Person;
import io.realm.exceptions.RealmMigrationNeededException;


public class RealmMigrationExampleActivity extends Activity {

    public static final String TAG = RealmMigrationExampleActivity.class.getName();

    private LinearLayout rootLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        String path0 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default0), "default0");
        String path1 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default1), "default1");
        String path2 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default2), "default2");

        try {
            // should throw as migration is required
            Realm.getInstance(this, "default0");
        } catch (RealmMigrationNeededException ex) {
            Log.i(TAG, "Excellent! This is expected.");
        }

        Realm.migrateRealmAtPath(path0, new Migration());
        Realm realm0 = Realm.getInstance(this, "default0");
        showStatus(realm0);

        Realm.migrateRealmAtPath(path1, new Migration());
        Realm realm1 = Realm.getInstance(this, "default1");
        showStatus(realm1);

        Realm.migrateRealmAtPath(path2, new Migration());
        Realm realm2 = Realm.getInstance(this, "default2");
        showStatus(realm2);
    }

    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String realmString(Realm realm) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Person person : realm.allObjects(Person.class)) {
            stringBuilder.append(person.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    private void showStatus(Realm realm) {
        String txt = realmString(realm);
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
