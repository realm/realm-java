package io.realm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class RealmBaseAdapter<T extends RealmObject> extends BaseAdapter {

    protected LayoutInflater inflater = null;

    protected RealmResults<T> rList;

    protected Context context = null;

    protected int resId = -1;

    public RealmBaseAdapter(Context context, RealmResults<T> rList) {
        this(context, -1, rList);
    }

    public RealmBaseAdapter(Context context, int resId, RealmResults<T> rList) {
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
    public abstract View getView(int i, View v, ViewGroup viewGroup);
}
