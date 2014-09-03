package io.realm.tests.examples.entities;


import java.lang.reflect.Field;
public final class DogRealmProxy extends Dog {

  private static int index_age;
  @Override
  public int getAge() {
    return (int)row.getLong( index_age );
  }
  @Override
  public void setAge(int value) {
    row.setLong( index_age, value );
  }

  private static int index_name;
  @Override
  public String getName() {
    return row.getString( index_name );
  }
  @Override
  public void setName(String value) {
    row.setString( index_name, value );
  }

  static {
    Field[] fields=Dog.class.getDeclaredFields();
    int i = 0;
    for (Field f : fields) {
      if (f.getName().compareTo("age") == 0) {
        index_age = i;
      }
      if (f.getName().compareTo("name") == 0) {
        index_name = i;
      }
      ++i;;
    }
  }
}
