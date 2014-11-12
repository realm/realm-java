# JSON API design document

This document is work-in-progress and describe the vision and proposed API's for working with JSON and Realm on Android. It was moved from the Wiki to here to ease discussion.

The current prototype can be found here: https://github.com/realm/realm-java/pull/489

## Vision
Realm should understand JSON so well that ~80% of all use-cases can be supported without needing 3rd party tools. For the last 20% people should either do it by hand or using GSON/Jackson.

The primary use case is dumping data from the network into Realm, this should be as easy as possible.


## Requirements

**High priority**
- Map between JSON naming and RealmObject naming.
- Select JSON nodes from a nested structure.
- Map many different date formats.
- Ignore JSON properties even though they exist in the model class.
- Handle different casing rules.
- Serialize RealmObject back to JSON (Core already has some native support for this?)

**Low priority**
- Select a single array node from a nested structure
- Value transformations:
	- Combine two strings with custom separator
	- Add two numbers together
	- Subtract two numbers
	- Others?


## 3rd party tools

The primary JSON libraries are GSON and Jackson. The primary blocker for not supporting them is that people are not able to add standalone objects to Realm. Once we fix this, I don't see any reason for not supporting both very easily.


## API

The proposed API consists of 2 ways of configuring the JSON mapping: Through annotations and/or through a RealmJSONMapper class. Annotations should cover most use cases while RealmJSONMapper should only be needed for special requirements.

This is the same approach that GSON has.

I will discuss the API from an example. So code first, explanations later:


**Input JSON**
````
{
    "status" : {
        "code" : 200
    },
    data :  {
        "fullName" : "John Doe",
        "address" "Founders House"
        "birth" : "2014-11-04T10:15:16+02:00",
        "animals: {
            "dogs" : [
                { "name" : "Fido" },
                { "name" : "Blackie" }
            ],
            "cats" : []
        }
    }
}
````

**Model class**
```
@NodePath("data")
public class Person extends RealmObject {

    @SerializedName("fullName")
    private String name;

    @SerializedDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    private Date birth;

    @NodePath("animals.dogs")
    private RealmList<Dog> dogs;

    @IgnoreNode
    private String address;

    // Standard getters and setters

}
````

**RealmJSONMapper**

Corresponding mapper for the above annotations.

````
RealmJSONMapper mapper = new RealmJSONMapper.Builder()
    .nameComparisonPolicy(NameComparisonPolicy.LOWERCASE)   
    .startFrom("data")
    .mapField("name", "fullName")
    .mapDate("birth", yyyy-MM-dd'T'HH:mm:ssZ)
    .useChildNode("dogs", "animals.dogs") 
    .ignoreField("address")
    .build()
````

**Realm API Methods**

````
// It looks like a lot of methods, but is essentially the combination of 3 dimensions
//
// Support for JSONObject, JSONArray, String and InputStream as input
// Support for create\* and createOrUpdate\*.
// Optional RealmJSONMapper

Realm.createObjectFromJson(Class realmObject, JSONObject json)
Realm.createObjectFromJson(Class realmObject, JSONObject json, RealmJSONMapper mapper)

Realm.createAllFromJson(Class realmObject, JSONArray json)
Realm.createAllFromJson(Class realmObject, JSONArray json, RealmJSONMapper mapper)

Realm.createObjectFromJson(Class realmObject, String json)
Realm.createObjectFromJson(Class realmObject, String json, RealmJSONMapper mapper)

Realm.createAllFromJson(Class realmObject, String json)
Realm.createAllFromJson(Class realmObject, String json, RealmJSONMapper mapper)


// Uses JsonReader for stream parsing. Only for API 11+
Realm.createObjectFromJson(Class realmObject, InputStream json);
Realm.createObjectFromJson(Class realmObject, InputStream json, RealmJSONMapper mapper);

Realm.createAllFromJson(Class realmObject, InputStream json);
Realm.createAllFromJson(Class realmObject, InputStream json, RealmJSONMapper mapper);

````


## Naming Policy

The default casing rule should either be STRICT or LOWERCASE ie, either json names and field names has to match or the comparison is done after lowercasing both. I would suggest LOWERCASE as that is the most forgiving.

GSON has a lot more options in a special class called FieldNamingPolicy. We should add something something similar in the RealmJSONMapper. I have called our NameComparisonPolicy as that seemed more appropriate.


## Mapping names

Mapping between JSON names and Java names should just follow our default naming policy, unless overriden by a @SerializedName annotation.This allow for fine grained control of different naming schemes.

@SerializedName is also used by GSON.


## Parsing dates

We want to support two types of dates: longs and Strings that can be parsed by a date specification.

Dates of type long are supported implicitly (ie. we do the conversion automatically). String dates are supported through the @SerializedDateFormat annotation.

We should allow all the same arguments as those specified here: http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html

@SerializedDateFormat is not used by GSON, but the similar name should hopefully make sense for people.


## Extracting child nodes

Used to extract a different node in the JSON hierarchy. This is useful if incoming JSON is not formatted nicely. I have seen JSON like my example in the wild, and by having this, people don't have to create unnecessary model classes, like they do with GSON currently.

The name @NodePath is a bit arbitrary and could probably use some iterations. Feedback welcome. 

Perhaps it could be combined with @SerializedName in order to reduce the number of annotations.


## Ignore JSON fields

Could be a useful feature. I havn't seen a use case though. @Ignore annotation also prevents importing the JSON value. 

@IgnoreNode should feel similar to @NodePath but it is still a bit arbitrary. 


## RealmJSONMapper

Should use the Builder pattern to build all the mapping rules used. Behind the scenes we should probably convert annotations to this as well, but by exposing it we can hide all kind of crazy options without polluting the rest of the API.

Names differ a bit from the annotations but I feel they flow better that way. Feedback on style/naming very welcome.


## Some discussion points

- Is it JSON or Json in API names?

- createAll vs. createObjects: createAll is similar to addAll from Lists in Java. I feel that createObject and createObjects are to similar, but input on this would be appreciated.

- Support for child JSONMappers on sub nodes? Ie. should it be possible to set a another Mapper class on any one childNode? Probably low priority.

- Any other requirements. iOS has had time to gather more feedback about their current solution?