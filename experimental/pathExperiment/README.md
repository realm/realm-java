## Path/EventBus interoperability test

This project tests how well Realm works with Paths' Android Priority Job Queue and greenrobot's EventBus

Path: https://github.com/path/android-priority-jobqueue

Eventbus: http://greenrobot.github.io/EventBus/

## Conclusion

- There should be no problems with Path as long as Realm interaction is contained within each Job.

- Eventbus work with the default settings, but care must be taken by users to not parse Realm objects
  between threads, ie. using onEventMainThread() instead of onEvent().