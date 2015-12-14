# Examples combining RxJava and Realm

# Things to keep in mind

- Observables might have a default `Scheduler` they operate on that is different than the one 
  the RealmObject was created on. Accessing Realm objects on the wrong thread will throw a 
  `IllegalStateException`.
  
- RealmObjects are live objects that automatically stay up to date. Observables that operate 
  on multiple "versions" will most likely not work as expected, e.g. `distinctUntilChanged`.

- Retrofit 1.x automatically offload to a worker thread.

- Use the Realm async API instead of `subscribeOn` to move Realm work off the UI thread.

- You can use `Realm.copyFromRealm` to make a copy of Realm data.
