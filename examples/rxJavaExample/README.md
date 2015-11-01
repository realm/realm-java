# Examples combining RxJava and Realm

# Things to keep in mind

- Observables might have a default Scheduler they operate on that is 
  different than the one the RealmObject was created on.
  
- RealmObjects auto-update. Observables that operate on multiple "versions" 
  will most likely not work as expected.

- Retrofit automatically offload to a worker thread.

- Use Realm async API instead of subscribeOn to offload Realm work.


## Resources (TODO)
- http://www.grahamlea.com/2014/07/rxjava-threading-examples/