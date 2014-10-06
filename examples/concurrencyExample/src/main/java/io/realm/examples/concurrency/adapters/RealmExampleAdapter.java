package io.realm.examples.concurrency.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.examples.concurrency.R;
import io.realm.examples.concurrency.model.Person;

public class RealmExampleAdapter<T extends RealmObject> extends BaseAdapter {

    private LayoutInflater inflater = null;

    protected RealmResults<T> rList;

    private Context context = null;

    private int resId = -1;

    public RealmExampleAdapter(Context context) {
        this(context, -1);
    }

    public RealmExampleAdapter(Context context, int resId) {
        this.resId = resId;
        this.context = context;
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

    // Can't use RealmResults
    public void setData(RealmResults<T> rList) {
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
