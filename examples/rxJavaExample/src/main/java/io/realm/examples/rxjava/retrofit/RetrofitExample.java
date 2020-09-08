/*
 * Copyright 2015 Realm Inc.
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

package io.realm.examples.rxjava.retrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class RetrofitExample extends AppCompatActivity {

    private Realm realm;
    private Disposable disposable;
    private ViewGroup container;
    private GitHubApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        container = findViewById(R.id.list);
        realm = Realm.getDefaultInstance();
        api = createGitHubApi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load all persons and merge them with their latest stats from GitHub (if they have any)
        disposable = realm.where(Person.class).isNotNull("githubUserName").sort("name").findAllAsync().asFlowable()
                // We only want the list once it is loaded.
                .filter(people -> people.isLoaded())
                .switchMap(people -> Flowable.fromIterable(people))

                // get GitHub statistics.
                .flatMap(person -> api.user(person.getGithubUserName()))

                // Map Network model to our View model
                .map(gitHubUser -> new UserViewModel(gitHubUser.name, gitHubUser.public_repos, gitHubUser.public_gists))

                // Retrofit put us on a worker thread. Move back to UI
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(user -> {
                    // Print user info.
                    TextView userView = new TextView(RetrofitExample.this);
                    userView.setText(
                            String.format(Locale.US, "%s : %d/%d", user.getUsername(), user.getPublicRepos(), user.getPublicGists()));
                    container.addView(userView);
                }, throwable -> throwable.printStackTrace());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private GitHubApi createGitHubApi() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(JacksonConverterFactory.create());

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        final String gitHubToken = ""; // Set GitHub OAuth token to avoid throttling if example is used a lot

        if (!isEmpty(gitHubToken)) {
            httpClientBuilder.addInterceptor(chain -> {
                Request originalRequest = chain.request();
                Request modifiedRequest = originalRequest
                        .newBuilder()
                        .header("Authorization", format("token %s", gitHubToken))
                        .method(originalRequest.method(), originalRequest.body())
                        .build();
                return chain.proceed(modifiedRequest);
            });
        }

        OkHttpClient httpClient = httpClientBuilder.build();
        builder.client(httpClient);
        return builder.build().create(GitHubApi.class);
    }
}
