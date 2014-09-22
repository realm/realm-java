package io.realm.examples.intro.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.realm.examples.intro.ContainerActivity;
import io.realm.examples.intro.R;

public class TileAdapter extends BaseAdapter {

    public static final String TAG = TileAdapter.class.getName();

    private LayoutInflater inflater;

    private Context context = null;

    public class RealmExample {
        public String descriptor;
        public String color;
        public Class type;
    }

    private List<RealmExample> examples = null;

    public TileAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<RealmExample> details) {
        this.examples = details;
    }

    @Override
    public int getCount() {
        if (examples == null) {
            return 0;
        }
        return examples.size();
    }

    @Override
    public Object getItem(int position) {
        if (examples == null || examples.get(position) == null) {
            return null;
        }
        return examples.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View currentView, ViewGroup parent) {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.grid_tile, parent, false);
        }

        final RealmExample ex = examples.get(position);

        ((TextView)currentView.findViewById(R.id.name)).setText(ex.descriptor);
        currentView.findViewById(R.id.name).setBackgroundColor(Color.parseColor(ex.color));

        currentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, ContainerActivity.class);
                intent.putExtra(ContainerActivity.FRAGMENT_NAME_EXTRA, ex.type);
                context.startActivity(intent);
            }
        });

        return currentView;
    }
}
