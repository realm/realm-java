package io.realm.examples.concurrency.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RealmArrayAdapter<E extends RealmObject> extends ArrayAdapter<E> {

    public static final String TAG = RealmArrayAdapter.class.getName();

    private int resId   = -1;
    private int fieldId = -1;

    private boolean notifyOnChange = true;

    private RealmList rList;

    private Context context = null;

    private LayoutInflater inflater = null;

    public RealmArrayAdapter(Context context, int resId, RealmList rList) {
        super(context, resId, rList);
        this.context = context;
        this.resId   = resId;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public RealmArrayAdapter(Context context, int resId, int fieldId, RealmList rList) {
        super(context, resId, fieldId, rList);
        this.context = context;
        this.resId   = resId;
        this.fieldId = fieldId;
    }

    @Override
    public int getCount() {
        return rList.size();
    }

    @Override
    public E getItem(int i) {
        super.getItem(i);
        if(rList.size() > i) {
            return (E)rList.get(i);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        View view;
        TextView text;

        if (v == null) {
            // Adapter fails if resId does not exist
            view = inflater.inflate(resId, null, false);
        } else {
            view = v;
        }

        try {
            if (fieldId == 0) {
                text = (TextView) view;
            } else {
                text = (TextView) view.findViewById(fieldId);
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "RealmArrayAdapter requires the resource ID to be a TextView", e);
        }

        RealmObject item = getItem(i);
        if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.toString());
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        notifyOnChange = true;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
    }

    // RealmLists are thread safe, so no locking should be required.

    public void add(E object) {
        rList.add(object);
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void addAll(Collection<? extends E> collection) {
        rList.addAll(collection);
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void addAll(E ... items) {
        for(E it : items) {
            rList.add(it);
        }
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void insert(E object, int index) {
        rList.add(index, object);
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void remove(E object) {
        rList.remove(object);
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void clear() {
        rList.clear();
        if (notifyOnChange) notifyDataSetChanged();
    }

    public void sort(Comparator<? super E> comparator) {
        // Collections.sort is a static helper method
        Collections.sort(rList, comparator);
        if (notifyOnChange) notifyDataSetChanged();
    }

    //TODO:  Add array filtering useful for search, etc.
}
