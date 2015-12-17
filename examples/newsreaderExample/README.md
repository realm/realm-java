NewsReader
-------------

This app is an example app that shows how Realm can be combined with a Model-View-Presenter (MVP) like architecture 
to create apps that work completely offline.

# Usage

This app is currently depending on experimental RxJava features in Realm, so the Realm plugin must be installed
in mavenLocal() before compiling the app. This is done from the main folder doing

    > ./gradlew installRealmJava
    
Then the app can be installed afterwards
    
    > cd examples
    > ./gradlew newsreaderExample:installDebug