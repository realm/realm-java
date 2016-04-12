package realm.io.storeencryptionpassword;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import realm.io.storeencryptionpassword.model.TodoItem;

class MyAdapter extends RealmBaseAdapter<TodoItem> implements ListAdapter {

    private static class ViewHolder {
        TextView name;
    }

    public MyAdapter(Context context, OrderedRealmCollection<TodoItem> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TodoItem item = adapterData.get(position);
        viewHolder.name.setText(item.getName());
        return convertView;
    }

    public OrderedRealmCollection<TodoItem> getAdapterData() {
        return adapterData;
    }
}
