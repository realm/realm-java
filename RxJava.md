Proposal for RxJava API

Starting to think about an API for this:


## API Proposal

###  PUBLIC API

All objects with RealmChangeListeners should also expose an Observable
```
Realm.observable()
RealmObject.observable()
RealmResults.observable()
RealmList.observable()
```

Calling any of these methods should detect if RxJava is actually on the classpath and throw a proper exception if not.
Note that just doing `realm.observable()` would not even compile in IntelliJ because it would not be able to import
the `rx.Observable` class.


Note, we don't expose an `observable()` on RealmQuery like e.g. SQLBrite does, this is not needed as your async
RealmResults actually have the same function while being semantically clearer. Listining to changes to a query is a bit 
odd, while changes to a query result makes a lot more sense.

It has the slight downside that you cannot defer work when creating the observable. You actually have to trigger the
query first. If this is deemed undesirable, we can expose the following methods on RealmQuery which will mimic our 
current `find*` methods.

```
RealmQuery.observeAll(); // = RealmQuery.findAllAsync();
RealmQuery.observeAllSorted(String name); 
RealmQuery.observeAllSorted(String name, boolean sortOrder); 
RealmQuery.observeAllSorted(String name, boolean sortOrder, String name2, boolean sortOrder2); 
RealmQuery.observeFirst(); 
```

### PRIVATE API
**RxObservableFactory**: Interface for all Rx implementations. Will make it easy to swap between RxJava1, RxJava2 if
  needed.

### CONFIGURING REALM
Realm supports both libraries and app code using Realm independantly. However due to class loading conflicts we should
not worry about a library wanting to use RxJava 1 while the app wanted to use RxJava 2. 

This means we should be able to detect automatically which version of RxJava is being used and instantiate the
appropriate factory without user involvement.

### Examples 

Examples of using this API can be found here:
https://github.com/realm/realm-java/pull/1710


-------------
Update 01/11-2015: Until something 

## How to include RxJava?

First concern is how to actually include RxJava. I see 3 solutions

** A. Plugin (old RxAndroid) **

```
// Gradle dependency
compile 'io.realm:realm-android:1.0.0'
compile 'io.realm:realm-android-rxjava:1.0.0'

// Usage
RealmResults<Person> realmResults = realm.where(Person.class).findAll();
Observable<RealmResults<Person>> observable = RxRealm.observable(realmResult);
```

Advantage is full flexibility for developers and a clear API. Downside is that 
the code becomes less fluent because you have to rely on static methods on the 
`RxRealm` helper class. 


** B. Opt-in (StorIO does this) ** 

```
// Gradle dependency
compile 'io.realm:realm-android:1.0.0'
compile 'compile 'io.reactivex:rxjava:1.0.8'

// Usage, can potentially crash
Observable<RealmResults<Person>> observerable = realm.where(Person.class).findAll().observable(); 
```

RxJava would only be a provided dependency for Realm. This means that calling 
`observable()` will crash unless the app provides the dependency. Advantage is 
that users that don't want RxJava doesn't have to do anything, downside is that 
the API exposes a method that crashes unless additional configuration is added 



** C. Full support / opt-out **

```
// Gradle
compile 'io.realm:realm-android:1.0.0'

// Opt-out
compile 'io.realm:realm-android:1.0.0' {
	exclude group: 'io.reactivex', module: 'rxjava'
}

// Usage
Observable<RealmResults<Person>> observable = realm.where(Person.class).findAll().observable();
```

Advantage is full support out of the box with minimal fuss. Downside is 
increased method limit and app size unless people manually remove the RxJava 
dependency. It also means we tie ourselves to RxJava with regard to future 
releases/compatibility etc.



** My thoughts ** 

I am probably learning most towards C). It will provide ease-of-use out of the 
box and we can add documentation to our website/examples on how to opt-out. If 
some competing reactive framework shows itself in the future, we can consider 
moving to a plug-in approach instead.


### RxJava 2 / RxMobile

We need to investigate any compatibility problems with RxJava2 and RxMobile. As 
far as I know, they have interface parity, but we need to verify this.


## API 

Assuming we implement either solution B) or C) from above, I would suggest the 
following API:

```
// All Realm API classes now has an additional `observable()` method for 
// returning an Rx Observable.
// This is a complement to our own ChangeListeners.

Realm.observable();
RealmResults.observable();
RealmObject.observable();
RealmList.observable();

// Example usage: Find and display the combined age of all persons using async 
// query.
realm.where(Person.class).findAllAsync().observable()
	.map(persons -> persons.sum("age"))
	.subscribe(combinedAge -> printSum(combinedAge))


// The above breaks the Rx contract slightly in the sense that work is 
// actually being done before the subscriber is attached, and it is not possible
// to decide on which thread the work is done. In order to support this we 
// need:

RealmQuery.observable()

// Example
RealmQuery query = new RealmQuery(realm, Person.class).equalTo("name", "foo");
Observable<RealmResults<Person>> observer = query.observable()
observer.subscribe(list -> doStuff(list));

// This would be equivalent to 
RealmResults results = realm.where(Person.class).equalTo("name", "foo").findAll();
results.observable().subscribe(list -> doStuff(list));


// ???
- Should RealmAsyncTask be observable?
- Do we need a RealmLifeCycleObserver so you can detect when a Realm is closed?
- What about findFirst()/sum()/min()/max()/avg()/sorted variants on RealmQuery?
- Anything else?

```

### Implementation details

- Build on top of our existing ChangeListeners.
- Each object is backed by a BehaviorSubject in order to push last change when 
  people subscribe. This also fits the philosophy taken by our async API.
- Due to Realm's auto-refresh Observables on Realm/RealmList/RealmResults/RealmObject are hot.
- RealmQuery Observables are cold.

We can already implement this today, with some downsides:

a) Thread confinement prevents use of `subscribeOn`/`observeOn` and other 
   Observables that use multiple threads.
b) Auto-updating objects means you have to be careful comparing objects. 
   Observables like `distinctUntilChanged` must be used on explicit fields, not 
   the whole object.
c) Lack of fine-grained notifications will trigger all live observers on each 
   commit.

a/b) will both be solved by introducing `freeze()` as discussed here: https://github.com/realm/realm-java/issues/1208
An observable would automatically do this as mutable objects in a event stream 
are bad design anyway.

c) will be solved when we introduce fine-grained notifications: 
   https://github.com/realm/realm-java/issues/989. Until then liberal use of 
   `distinctUntilChanged` or similar will have to be used to prevent updating 
   the UI needlesly. 

Summary: We can add RxJava support today, but need to list a number of caveats. 
These caveats will automatically be reduced in future releases without effecting 
the Observable API. When both #1208 and #989 are implemented we will have full 
RxJava support.



