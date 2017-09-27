# Using this example

This example is a minimal demonstration of how to connect to and use the
Realm Object Server to synchronize changes between devices.

The example assumes that the Object Server is running on the machine
that built the application: The build machine IP address is automatically
injected into the build configuration.

To use a different ObjectServer, simply put the server IP Address into
the `build.gradle`, as indicated in the comments, on the lines like this:

    buildConfigField "String", "OBJECT_SERVER_IP", "\"${host}\""

For instance:

    buildConfigField "String", "OBJECT_SERVER_IP", "192.168.0.1"

To read more about the Realm Object Server and how to deploy it, see
https://realm.io/news/introducing-realm-mobile-platform/
