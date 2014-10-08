package io.realm.examples.realmadapters.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import io.realm.examples.realmadapters.R;
import io.realm.examples.realmadapters.model.Person;

// This is a concrete implementation where it is assumed that the use of
// the adapter is for Person objects
public class RealmPersonExampleAdapter extends RealmBaseAdapter<Person> {

    public RealmPersonExampleAdapter(Context context, int resId, RealmResults<Person> rList) {
        super(context, resId, rList, true);
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        View view;

        if (v == null) {
            // Adapter fails if resId does not exist
            view = inflater.inflate(resId, null, false);
        } else {
            view = v;
        }

        Person item = (Person)getItem(i);

        TextView tv = (TextView)view.findViewById(R.id.field1);
        tv.setText(item.toString());

        return view;
    }
}
