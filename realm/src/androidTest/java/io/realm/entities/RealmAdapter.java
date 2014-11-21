package io.realm.entities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class RealmAdapter extends RealmBaseAdapter<AllTypes> implements ListAdapter {

    private static class ViewHolder {
        TextView textView;
    }

    public RealmAdapter(Context context, RealmResults<AllTypes> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AllTypes item = realmResults.get(position);
        viewHolder.textView.setText(item.getColumnString());
        return convertView;
    }

    public RealmResults<AllTypes> getRealmResults() {
        return realmResults;
    }
}
