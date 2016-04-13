package realm.io.storeencryptionpassword;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import realm.io.storeencryptionpassword.model.TodoItem;

public class SecretTodoList extends ListActivity {

    private Realm realm;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random();

        realm = getRealm();
        if (realm == null) {
            finish();
            return;
        }
        RealmResults<TodoItem> todos = realm.where(TodoItem.class).findAll();
        setContentView(R.layout.secret_todo);
        final MyAdapter adapter = new MyAdapter(this, todos);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TodoItem timeStamp = adapter.getAdapterData().get(i);
                realm.beginTransaction();
                timeStamp.deleteFromRealm();
                realm.commitTransaction();
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.beginTransaction();
                TodoItem todoItem = realm.createObject(TodoItem.class);
                todoItem.setName("Item " + random.nextInt());
                realm.commitTransaction();
            }
        });

        setListAdapter(adapter);
    }

    private Realm getRealm() {
        try {
            return Realm.getDefaultInstance();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Please unlock Realm first.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
