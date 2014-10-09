package io.realm;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.jetbrains.annotations.NotNull;

public abstract class RealmBaseAdapter<T extends RealmObject> extends BaseAdapter {

    protected LayoutInflater inflater;
    protected RealmResults<T> realmResults;
    protected Context context;
    protected int resId;

    public RealmBaseAdapter(@NotNull Context context, int resId, @NotNull RealmResults<T> realmResults, boolean automaticUpdate) {
        this.resId   = resId;
        this.context = context;
        this.realmResults = realmResults;
        this.inflater = LayoutInflater.from(context);
        if (automaticUpdate) {
            realmResults.getRealm().addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getCount() {
        return realmResults.size();
    }

    @Override
    public T getItem(int i) {
        return realmResults.get(i);
    }

    @Override
    @Deprecated
    public long getItemId(int i) {
        return i; // TODO: find better solution
    }

    /**
     * Update the RealmResults associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature
     * @param realmResults the new RealmResults coming from the new query.
     */
    public void updateRealmResults(RealmResults<T> realmResults) {
        this.realmResults = realmResults;
        notifyDataSetChanged();
    }
}
