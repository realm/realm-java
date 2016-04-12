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

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class RetrofitExample extends Activity {

    private Realm realm;
    private Subscription subscription;
    private ViewGroup container;
    private GithubApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        container = (ViewGroup) findViewById(R.id.list);
        realm = Realm.getDefaultInstance();
        api = createGitHubApi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load all persons and merge them with their latest stats from GitHub (if they have any)
        subscription = realm.where(Person.class).isNotNull("githubUserName").findAllSortedAsync("name").asObservable()
                .filter(new Func1<RealmResults<Person>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<Person> persons) {
                        // We only want the list once it is loaded.
                        return persons.isLoaded();
                    }
                })
                .flatMap(new Func1<RealmResults<Person>, Observable<Person>>() {
                    @Override
                    public Observable<Person> call(RealmResults<Person> persons) {
                        // Emit each person individually
                        return Observable.from(persons);
                    }
                })
                .flatMap(new Func1<Person, Observable<GitHubUser>>() {
                    @Override
                    public Observable<GitHubUser> call(Person person) {
                        // get GitHub statistics. Retrofit automatically does this on a separate thread.
                        return api.user(person.getGithubUserName());
                    }
                })
                .map(new Func1<GitHubUser, UserViewModel>() {
                    @Override
                    public UserViewModel call(GitHubUser gitHubUser) {
                        // Map Network model to our View model
                        return new UserViewModel(gitHubUser.name, gitHubUser.public_repos, gitHubUser.public_gists);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) // Retrofit put us on a worker thread. Move back to UI
                .subscribe(new Action1<UserViewModel>() {
                    @Override
                    public void call(UserViewModel user) {
                        // Print user info.
                        TextView userView = new TextView(RetrofitExample.this);
                        userView.setText(String.format(Locale.US, "%s : %d/%d",
                                user.getUsername(), user.getPublicRepos(), user.getPublicGists()));
                        container.addView(userView);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private GithubApi createGitHubApi() {

        RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint("https://api.github.com/");

        final String githubToken = ""; // Set GitHub OAuth token to avoid throttling if example is used a lot
        if (!isEmpty(githubToken)) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", format("token %s", githubToken));
                }
            });
        }

        return builder.build().create(GithubApi.class);
    }
}
