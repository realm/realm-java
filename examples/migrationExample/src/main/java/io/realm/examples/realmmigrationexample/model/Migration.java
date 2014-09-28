package io.realm.examples.realmmigrationexample.model;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class Migration implements RealmMigration {
    @Override
    public long execute(Realm realm, long version) {
        if (version == 0) {
            Table personTable = realm.getTable(Person.class);

            long fistNameIndex = getIndexForProperty(personTable, "firstName");
            long lastNameIndex = getIndexForProperty(personTable, "lastName");
            long fullNameIndex = personTable.addColumn(ColumnType.STRING, "fullName");
            for (int i = 0; i < personTable.size(); i++) {
                personTable.setString(fullNameIndex, i, personTable.getString(fistNameIndex, i) + " " +
                        personTable.getString(lastNameIndex, i));
            }
            personTable.removeColumn(getIndexForProperty(personTable, "firstName"));
            personTable.removeColumn(getIndexForProperty(personTable, "lastName"));
            version++;
        }
        if (version == 1) {
            Table personTable = realm.getTable(Person.class);
            Table petTable = realm.getTable(Pet.class);
            petTable.addColumn(ColumnType.STRING, "name");
            petTable.addColumn(ColumnType.STRING, "type");
            long petsIndex = personTable.addColumnLink(ColumnType.LINK_LIST, "pets", petTable);
            long fullNameIndex = getIndexForProperty(personTable, "fullName");

            for (int i = 0; i < personTable.size(); i++) {
                if (personTable.getString(fullNameIndex, i).equals("JP McDonald")) {
                    personTable.getRow(i).getLinkList(petsIndex).add(petTable.add("Jimbo", "Dog"));
                }
            }
            version++;
        }
        if (version == 2) {
            Table petTable = realm.getTable(Pet.class);
            long oldTypeIndex = getIndexForProperty(petTable, "type");
            long typeIndex = petTable.addColumn(ColumnType.INTEGER, "type");
            for (int i = 0; i < petTable.size(); i++) {
                String type = petTable.getString(oldTypeIndex, i);
                if (type.equals("Dog")) {
                    petTable.setLong(typeIndex, i, 1);
                }
                else if (type.equals("Cat")) {
                    petTable.setLong(typeIndex, i, 2);
                }
                else if (type.equals("Hamster")) {
                    petTable.setLong(typeIndex, i, 3);
                }
            }
            petTable.removeColumn(oldTypeIndex);
            version++;
        }
        return version;
    }

    private long getIndexForProperty(Table table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}

// Old data models
/* V0
@interface Person : RLMObject
@property NSString *firstName;
@property NSString *lastName;
@property int age;
@end
 */

/* V1
@interface Person : RLMObject
@property NSString *fullName;   // combine firstName and lastName into single field
@property int age;
@end
*/

/* V2
@interface Pet : RLMObject      // add a new model class
@property NSString *name;
@property NSString *type;
@end
RLM_ARRAY_TYPE(Pet)

@interface Person : RLMObject
@property NSString *fullName;
@property int age;
@property RLMArray<Pet> *pets;  // add an array property
@end
*/

/* V3
@interface Pet : RLMObject
@property NSString *name;
@property int type;             // type becomes int
@end
RLM_ARRAY_TYPE(Pet)

@interface Person : RLMObject
@property NSString *fullName;
@property RLMArray<Pet> *pets;  // age and pets re-ordered
@property int age;
@end
*/