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

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        disposable = realm.where(Person.class).isNotNull("githubUserName").findAllSortedAsync("name").asFlowable()
                .filter(new Predicate<RealmResults<Person>>() {
                    @Override
                    public boolean test(RealmResults<Person> people) throws Exception {
                        // We only want the list once it is loaded.
                        return people.isLoaded();
                    }
                })
                .flatMap(new Function<RealmResults<Person>, Publisher<Person>>() {
                    @Override
                    public Publisher<Person> apply(RealmResults<Person> people) throws Exception {
                        return Flowable.fromIterable(people);
                    }
                })
                .flatMap(new Function<Person, Publisher<GitHubUser>>() {
                    @Override
                    public Publisher<GitHubUser> apply(Person person) throws Exception {
                        // get GitHub statistics.
                        return api.user(person.getGithubUserName());
                    }
                })
                .map(new Function<GitHubUser, UserViewModel>() {
                    @Override
                    public UserViewModel apply(GitHubUser gitHubUser) throws Exception {
                        // Map Network model to our View model
                        return new UserViewModel(gitHubUser.name, gitHubUser.public_repos, gitHubUser.public_gists);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) // Retrofit put us on a worker thread. Move back to UI
                .subscribe(new Consumer<UserViewModel>() {
                    @Override
                    public void accept(UserViewModel user) throws Exception {
                        // Print user info.
                        TextView userView = new TextView(RetrofitExample.this);
                        userView.setText(String.format(Locale.US, "%s : %d/%d",
                                user.getUsername(), user.getPublicRepos(), user.getPublicGists()));
                        container.addView(userView);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
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

        final String githubToken = ""; // Set GitHub OAuth token to avoid throttling if example is used a lot

        if (!isEmpty(githubToken)) {
            httpClientBuilder.addInterceptor(new Interceptor() {

                @Override
                public Response intercept(@ParametersAreNonnullByDefault Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    Request modifiedRequest = originalRequest
                            .newBuilder()
                            .header("Authorization", format("token %s", githubToken))
                            .method(originalRequest.method(), originalRequest.body())
                            .build();
                    return chain.proceed(modifiedRequest);
                }
            });
        }

        OkHttpClient httpClient = httpClientBuilder.build();
        builder.client(httpClient);
        return builder.build().create(GitHubApi.class);
    }
}
