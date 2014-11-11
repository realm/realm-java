## Path/EventBus interoperability test

This project tests how well Realm works with Paths' Android Priority Job Queue and greenrobot's EventBus

Path: https://github.com/path/android-priority-jobqueue

Eventbus: http://greenrobot.github.io/EventBus/

## Conclusion

- There should be no problem with Path as long as Realm interaction is contained within each Job.

- Eventbus works fine with the default settings, but care must be taken by users not to pass Realm objects
  between threads, ie. using onEventMainThread() instead of onEvent().
