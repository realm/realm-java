package io.realm.examples.intro;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import io.realm.examples.intro.adapters.TileAdapter;
import io.realm.examples.intro.fragments.BasicExampleFragment;
import io.realm.examples.intro.fragments.ComplexExampleFragment;

public class RealmIntroExampleActivity extends Activity {

    public static final String TAG = RealmIntroExampleActivity.class.getName();

    private TileAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_examples);

        mAdapter = new TileAdapter(this);
        List<TileAdapter.RealmExample> list = buildExamples();
        mAdapter.setData(list);

        GridView gridView = (GridView) findViewById(R.id.examples_list);
        gridView.setVerticalSpacing(10);
        gridView.setHorizontalSpacing(10);
        gridView.setAdapter(mAdapter);
    }

    private List<TileAdapter.RealmExample> buildExamples() {
        List<TileAdapter.RealmExample> list = new ArrayList<TileAdapter.RealmExample>();

        TileAdapter.RealmExample example = mAdapter.new RealmExample();
        example.descriptor = "Basic";
        example.color = "#ff3333";
        example.type = BasicExampleFragment.class;
        list.add(example);

        example = mAdapter.new RealmExample();
        example.descriptor = "Complex";
        example.color = "#338833";
        example.type = ComplexExampleFragment.class;
        list.add(example);

        return list;
    }
}
