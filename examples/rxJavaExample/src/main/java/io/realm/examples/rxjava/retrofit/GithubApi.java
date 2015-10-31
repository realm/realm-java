package io.realm.examples.rxjava.retrofit;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * GitHub API definition
 */
public interface GithubApi {
    /**
     * See https://developer.github.com/v3/users/
     */
    @GET("/users/{user}")
    Observable<GitHubUser> user(@Path("user") String user);
}