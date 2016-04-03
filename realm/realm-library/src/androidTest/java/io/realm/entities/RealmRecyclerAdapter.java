package io.realm.entities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.RealmBaseRecyclerAdapter;
import io.realm.RealmResults;

public class RealmRecyclerAdapter extends RealmBaseRecyclerAdapter<AllTypes, RealmRecyclerAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(final View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public RealmRecyclerAdapter(final Context context, final RealmResults<AllTypes> realmResults, final boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        AllTypes item = getItem(position);
        holder.textView.setText(item.getColumnString());
    }
}
