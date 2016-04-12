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

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import realm.io.storeencryptionpassword.model.TodoItem;

/**
 * Created by Nabil on 12/04/2016.
 */
public class SecretTodoList extends ListActivity {

    private Realm realm;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random(System.currentTimeMillis());

        realm = Realm.getDefaultInstance();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
