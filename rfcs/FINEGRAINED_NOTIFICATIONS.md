# Finegrained notifications

## Summary

This document describes an API proposal for a finegrained collection for Realm Java. Object-level notifications will be covered in another document.

## Motivation

Realm wants to promote a reactive pattern to working with your data. It should be possible to define a query for any change you are interested in and just depend on Realm figuring out when that change has happend and notify you. 

This is interesting for a number of use cases:

1) I want to animate or refresh a RecyclerView when elements are added, moved, modified or deleted in a `RealmResults`.
2) I want to be notified about the result of an query changing so I can refresh my UI.
3) I want my changelisteners to _only_ trigger when there is a relevant change.

While you in some use cases probably want to compare old and new data, we have not yet found a compelling enough use case to support this for collections.
Single object notifications will have this functionality.

## Current situation

- Right now Realm Java changelisteners are "table-based", which means that a change to any element in a table will trigger all changelisteners registered on that table, even though the element wasn't included in the query. This results in a lot of un-needed change events being triggered.

- Single object change listeners work the same way, which is even worse.

- Changelisteners are not supported on RealmList

- Cocoa has support for fine-grained collection changes.

- `Realm` / `RealmResults` / `RealmObject` all have methods like `addChangeListener/removeChangeListener/removeAllListeners`.


## Detailed design

Add these new interfaces and class:

```
// New generic interface for generic changes (= our current changelisteners)
// All relevant classes should implement this: Realm, RealmResults, RealmList, RealmObject, DynamicRealm, DynamicRealmObject
// TODO: Is Observable the best name?
public interface Observable<T> {
    void addChangeListener(RealmChangeListener<T> listener);
    void removeChangeListener(RealmChangeListener<T> listener);
    void removeAllChangeListeners(); // Current name is removeChangeListeners? IMO this name is better, but do we want the penalty of a rename?
}

// New interface for collections. Add another changelistener for fine-grained notifications
public interface ObservableCollection<T, L> extends Observable<T> {
    void addChangeListener(L listener);
    void removeChangeListener(L listener);
}

// New listener interface for fine-grained callback. Changes are encapsulated in the OrderedCollectionChange object
// Using OrderedCollectionChange to prepare for change events from unordered data structures.
public interface OrderedRealmCollectionChangeListener<T> {
    void onChange(T collection, OrderedCollectionChange changes);
}

// Class for describing changes
// Primary purpose is to make RecyclerView animations easy, but should also be general purpose
// RecyclerView uses the `get*Ranges()` methods.
public class OrderedCollectionChange {
    public long[] getDeletions() { return null; }
    public long[] getInsertertions() { return null; }
    public long[] getChanges()  { return null; }
    public Range[] getDeletionRanges() { return null; }
    public Range[] getInsertionRanges() { return null; }
    public Range[] getChangeRanges() { return null; }
    Move[] getMoves() { return null; }

    public static class Range {
        public final long startIndex;
        public final long length;

        public Range(long startIndex, long length) {
            this.startIndex = startIndex;
            this.length = length;
        }
    }

    public static class Move {
        public final long oldIndex;
        public final long newIndex;

        public Move(long oldIndex, long newIndex) {
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
    }
}
```

Following changes will be made to existing classes 

* `RealmObject` will implement `Observable`
* `Realm` will implement `Observable`
* `DynamicRealm` will implement `Observable`
* `DynamicRealmObject` will implement `Observable`
* `RealmResults` will implement `ObservableCollection`
* `RealmList` will implement `ObservableCollection`


### Future features that might impact this

* Object-level notifications: Will probably be another interface that extends `Oberservable`
* Unordered data sets: ObservableCollection does not put any restrictions there, and we can create `UnorderedCollectionChange` objects and `UnorderedRealmCollectionChangeListener` interfaces. This can be done without breaking changes.
* Changes to what type of changes we want to expose. Since `OrderedCollectionChange` is a class and not an interface, extending it with new functionality can be done without breaking changes.

## Work required

- [ ] We need to move `RealmResults` to be backed by `Results` from Object Store instead. 
- [ ] Figure out how to map OS changeset notification to Java equivalent. Cocoa current model moves as delete/insert, due to time constraints we might have to do the same initially. 
- [ ] Add interface to all relevant classes + implementation
- [ ] Deprecate `removeChangeListeners()`
- [ ] Benchmark DiffUtil vs. Realm. There are fundemental differences in architecture, but the question will come up. 



## Other ressources

- Cocoa PR: https://github.com/realm/realm-cocoa/pull/3359
- Cocoa docs: https://realm.io/docs/objc/latest/#collection-notifications