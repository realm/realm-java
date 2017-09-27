/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.newsreader.ui.details;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.examples.newsreader.R;
import io.realm.examples.newsreader.model.Model;
import io.realm.examples.newsreader.model.entity.NYTimesStory;

public class DetailsActivity extends AppCompatActivity {

    private static final String KEY_STORY_ID = "key.storyId";

    @BindView(R.id.details_text) TextView detailsView;
    @BindView(R.id.read_text) TextView readView;
    @BindView(R.id.date_text) TextView dateView;
    @BindView(R.id.loader_view) ProgressBar loaderView;

    private Toolbar toolbar;
    private DetailsPresenter presenter;

    public static Intent getIntent(Context context, NYTimesStory story) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(KEY_STORY_ID, story.getUrl());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup initial views
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loaderView.setVisibility(View.VISIBLE);

        // After setup, notify presenter
        String storyId = getIntent().getExtras().getString(KEY_STORY_ID);
        presenter = new DetailsPresenter(this, Model.getInstance(), storyId);
        presenter.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    public void showLoader() {
        loaderView.setAlpha(1.0f);
        loaderView.setVisibility(View.VISIBLE);
    }

    public void hideLoader() {
        if (loaderView.getVisibility() != View.GONE) {
            loaderView.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loaderView.setVisibility(View.GONE);
                }
            });
        }
    }

    public void showStory(NYTimesStory story) {
        toolbar.setTitle(story.getTitle());
        detailsView.setText(story.getStoryAbstract());
        dateView.setText(story.getPublishedDate());
    }

    public void setRead(boolean read) {
        if (read) {
            readView.setText(R.string.read);
            readView.animate().alpha(1.0f);
        } else {
            readView.setText("");
            readView.animate().alpha(0f);
        }
    }
}
