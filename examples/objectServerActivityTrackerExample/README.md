# Object Server Example 

This project contains a demo app demonstrating how you can build a real world app using modern
Android components and frameworks together with Realm Sync.

This demo app covers the use case of managing people who have booked an activity during a vacation.

It makes it possible for the person in charge of the activity to handle checkins as well.


## Requirements

Two Query-based reference Realms must exist on the server. These must be called:

* `/demo1`
* `/demo2`

They can be created using an Admin user through Realm Studio.

## Build project

1) Edit `io.realm.examples.objectserver.activitytracker.Constants` and set the proper URL to the Realm Sync server
2) Compile and install the app `./gradlew clean installDebug`

## Technical Details

The app is built using the following frameworks/libraries:

* Kotlin 1.3
* Databinding
* Android Architecture Components: LiveData and ViewModel
* RxJava
* Realm Java

For the UI parts the project uses package-by-feature instead of package-by-layer, so e.g. everything
related to the Excursion selection screen should be in the `io.realm.examples.objectserver.activitytracker.ui.excursionlist`
package.

The project uses an MVVM architecture. All logic related to controlling the UI should be in the 
various `*ViewModel` classes. These classes expose data using `LiveData` which are consumed by 
the `Activity` using data binding.

As this demo is relatively simple in scope, all business logic are contained within the view model 
classes, this also includes all usages of Realm.

