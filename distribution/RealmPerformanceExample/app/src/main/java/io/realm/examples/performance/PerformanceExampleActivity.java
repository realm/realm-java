package io.realm.examples.performance;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import io.realm.examples.performance.ormlite.ORMLiteFragment;
import io.realm.examples.performance.realm.RealmFragment;
import io.realm.examples.performance.sqlite.SQLiteFragment;
import io.realm.examples.performance.sugar_orm.SugarORMFragment;

public class PerformanceExampleActivity extends Activity {

    public static final String TAG = PerformanceExampleActivity.class.getName();

    private TileAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_test);

        mAdapter = new TileAdapter(this);
        List<TileAdapter.PerformanceTestExample> list = buildExamples();
        mAdapter.setData(list);

        GridView gridView = (GridView) findViewById(R.id.examples_list);
        gridView.setVerticalSpacing(10);
        gridView.setHorizontalSpacing(10);
        gridView.setAdapter(mAdapter);
    }

    private List<TileAdapter.PerformanceTestExample> buildExamples() {
        List<TileAdapter.PerformanceTestExample> list = new ArrayList<TileAdapter.PerformanceTestExample>();

        TileAdapter.PerformanceTestExample example = mAdapter.new PerformanceTestExample();
        example.descriptor = "Sugar ORM";
        example.color = "#ff3333";
        example.type = SugarORMFragment.class;
        list.add(example);

        example = mAdapter.new PerformanceTestExample();
        example.descriptor = "SQLite";
        example.color = "#aa33aa";
        example.type = SQLiteFragment.class;
        list.add(example);

        example = mAdapter.new PerformanceTestExample();
        example.descriptor = "ORMLite";
        example.color = "#338833";
        example.type = ORMLiteFragment.class;
        list.add(example);

        example = mAdapter.new PerformanceTestExample();
        example.descriptor = "Realm";
        example.color = "#3333aa";
        example.type = RealmFragment.class;
        list.add(example);

        example = mAdapter.new PerformanceTestExample();
        example.descriptor = "User Selected";
        example.color = "#ffaa00";
        example.type = UserSelectedTestsFragment.class;
        list.add(example);

        return list;
    }
}
