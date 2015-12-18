# Contributing

## Filing Issues

Whether you find a bug, typo or an API call that could be clarified, please [file an issue](https://github.com/realm/realm-java/issues) on our GitHub repository.

When filing an issue, please provide as much of the following information as possible in order to help us fix it:

1. **Goals**
2. **Expected results**
3. **Actual results**
4. **Steps to reproduce**
5. **Code sample that highlights the issue** (link to full Android Studio projects that we can compile ourselves are ideal)
6. **Version of Realm/Android Studio/OS**

If you'd like to send us sensitive sample code to help troubleshoot your issue, you can email <help@realm.io> directly.

## Contributing Enhancements

We love contributions to Realm! If you'd like to contribute code, documentation, or any other improvements, please [file a Pull Request](https://github.com/realm/realm-java/pulls) on our GitHub repository. Make sure to accept our [CLA](#CLA)!

### CLA

Realm welcomes all contributions! The only requirement we have is that, like many other projects, we need to have a [Contributor License Agreement](https://en.wikipedia.org/wiki/Contributor_License_Agreement) (CLA) in place before we can accept any external code. Our own CLA is a modified version of the Apache Software Foundation’s CLA.

[Please submit your CLA electronically using our Google form](https://docs.google.com/forms/d/1bVp-Wp5nmNFz9Nx-ngTmYBVWVdwTyKj4T0WtfVm0Ozs/viewform?fbzx=4154977190905366979) so we can accept your submissions. The GitHub username you file there will need to match that of your Pull Requests. If you have any questions or cannot file the CLA electronically, you can email <help@realm.io>.

## Unit tests

All PR's must be accompanied by related unit tests.

## Code style

While we havn't described our code style yet, please just follow the existing style you see in the files you change.

## Javadoc

All public classes and methods must have Javadoc describing their purpose.

```java
/**
 * Checks if given field is equal to the provided value. 
 *
 * @param fieldName the field to compare.
 * @param fieldValue the value to compare with.
 * @param caseSensitive if {@code true}, substring matching is case sensitive. Setting this to {@code false} works for English locale characters only.
 * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
 *                      locale characters.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
 * @throws IllegalArgumentException if field name doesn't exists, it doesn't contain a list of links or the type
 * of the object represented by the DynamicRealmObject doesn't match.
 * @deprecated Please use {@link #average(String)} instead.
 * @see #endGroup()
 */
public RealmQuery<E> equalTo(String fieldName, String fieldValue, boolean caseSensitive) {
  // ...
}
```

* Method descriptions begin with a verb phrase, e.g. "Checks" instead of "Check". 
* Capitalize the first letter of the method and @deprecated descriptions. Everything else starts with lower case.
* Empty line between method description and the rest.
* End all descriptions with a period `.` (except @see).
* Reference other Realm classes using `{@link ...}`.
* Wrap Java values in `{@code ...}`.
* @throws description must start with "if".
* Never list generic exceptions like `RuntimeException`, `Exception` or `Error`. Always reference the specific error.
* Line-length maximum is 120 chars. Parameter descriptions that go above this, should be split into multiple lines and indented. Otherwise do not use indentation (contrary to Oracle guidelines).

Above is based on the official guidelines from Oracle regarding Javadoc: http://www.oracle.com/technetwork/articles/java/index-137868.html

