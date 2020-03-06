# Unit test app

This app is a skeleton app that can be used when debugging native code. 
It is a work-around until https://issuetracker.google.com/issues/143095235 is fixed.

This bug prevents the native debugger to work with library projects. So if you want to debug
a native problem. The code needs to be copied to here. From where the debugger will work.

Note, in order to allow the debugger to work. Code has to run inside the app, not as a unit test.
So all code should be copied to `MainActivity.kt`