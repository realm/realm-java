package io.realm.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.realm.example.entities.User;
import io.realm.testApp.R;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return this.users.size();
    }

    @Override
    public Object getItem(int i) {
        return this.users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(this.context);
            view = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        TextView itemView = (TextView)view.findViewById(R.id.textView);

        itemView.setText(this.users.get(i).getName());


        return view;
    }
}
