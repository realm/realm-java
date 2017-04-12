# Retrofit 2
[Retrofit] is a library from [Square] that makes it easy to work with a REST API in a typesafe manner.

With Retrofit 2, [GSON] is no longer used by default, but can be used via it's converter module.
If you want to deserialize network JSON data to RealmObjects with GSON, you must add a properly configured GsonConverter.

```java
Gson gson = new GsonBuilder()
        .setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass().equals(RealmObject.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        })
        .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create(gson)
                .build();

GitHubService service = retrofit.create(GitHubService.class);
```
Retrofit does not automatically add objects to Realm, instead you must manually add them using the realm.copyToRealm() or realm.copyToRealmOrUpdate() method.
```java
GitHubService service = retrofit.create(GitHubService.class);

service.listRepos().enqueue(new Callback<RealmList<Repo>>() {
            @Override
            public void onResponse(Response<RealmList<Repo>> response, Retrofit retrofit) {
                // copy the objects to realm
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().copyToRealmOrUpdate(response.body());
                Realm.getDefaultInstance().commitTransaction();
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
```
[Square]: <http://square.github.io/>
[Retrofit]: <http://square.github.io/retrofit/>
[GSON]: <https://realm.io/docs/java/latest/#gson>
[realm.copyToRealm()]: <https://realm.io/docs/java/latest/api/io/realm/Realm.html#copyToRealm-java.lang.Iterable>
[realm.copyToRealmOrUpdate()]: <https://realm.io/docs/java/latest/api/io/realm/Realm.html#copyToRealmOrUpdate-java.lang.Iterable->
