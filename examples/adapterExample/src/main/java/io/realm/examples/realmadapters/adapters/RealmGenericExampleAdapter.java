package io.realm.examples.realmadapters.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.R;

// This is labeled Generic because for simple cases the typing is left generic
public class RealmGenericExampleAdapter<T extends RealmObject> extends RealmBaseAdapter<T> {

    public RealmGenericExampleAdapter(Context context, RealmResults<T> rList) {
        super(context, rList);
    }

    public RealmGenericExampleAdapter(Context context, int resId, RealmResults<T> rList) {
        super(context, resId, rList);
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

        RealmObject item = getItem(i);

        TextView tv = (TextView)view.findViewById(R.id.field1);
        tv.setText(item.toString());

        return view;
    }
}
