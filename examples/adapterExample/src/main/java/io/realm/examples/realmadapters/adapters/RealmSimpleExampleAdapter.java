package io.realm.examples.realmadapters.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.R;

// This is labeled simple because basically the user ignores RealmBaseAdapter and
// just builds what they want
public class RealmSimpleExampleAdapter<T extends RealmObject> extends BaseAdapter {

    private LayoutInflater inflater = null;

    protected RealmResults<T> rList;

    private Context context = null;

    private int resId = -1;

    public RealmSimpleExampleAdapter(Context context, RealmResults<T> rList) {
        this(context, -1, rList);
    }

    public RealmSimpleExampleAdapter(Context context, int resId, RealmResults<T> rList) {
        this.resId   = resId;
        this.context = context;
        this.rList   = rList;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(rList == null)
            return 0;
        return rList.size();
    }

    @Override
    public RealmObject getItem(int i) {
        if(rList == null)
            return null;
        return rList.get(i);
    }

    @Override
    public long getItemId(int i) {
        if(rList == null)
            return -1;
        return i;
    }

    // This method should only be called if you change the query you are using to generate the RealmResults
    public void updateRealmResults(RealmResults<T> rList) {
        this.rList = rList;
        notifyDataSetChanged();
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
