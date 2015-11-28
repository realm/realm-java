# Examples combining RxJava and Realm

# Things to keep in mind

- Observables might have a default Scheduler they operate on that is 
  different than the one the RealmObject was created on.
  
- RealmObjects are live objects that automatically staty up to date. Observables that operate on multiple "versions"
  will most likely not work as expected.

- Retrofit automatically offload to a worker thread.

- Use Realm async API instead of subscribeOn to offload Realm work.

- You can use Realm.copyFromRealm to make a copy of Realm data




## Resources (TODO)
- http://www.grahamlea.com/2014/07/rxjava-threading-examples/