package io.realm.realmintroexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove old files
        File f;
        f = new File(this.getFilesDir()+"/default.realm");
        f.delete();

        f = new File(this.getFilesDir()+"/default.realm.lock");
        f.delete();

        // Run examples
        IntroExample ie = new IntroExample(this);
        try {
            ie.WriteAndRead();
            ie.QueryYourObjects();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
