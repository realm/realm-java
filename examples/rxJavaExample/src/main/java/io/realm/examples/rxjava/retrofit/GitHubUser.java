package io.realm.examples.rxjava.retrofit;

/**
 * Model class for GitHub users: https://developer.github.com/v3/users/#get-a-single-user
 */
public class GitHubUser {
    public String name;
    public String email;
    public int public_repos;
    public int public_gists;
}
