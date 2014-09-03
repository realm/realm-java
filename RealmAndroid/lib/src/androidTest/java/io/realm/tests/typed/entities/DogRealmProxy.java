package io.realm.tests.typed.entities;


import java.lang.reflect.Field;
public final class DogRealmProxy extends Dog {

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
      if (f.getName().compareTo("name") == 0) {
        index_name = i;
      }
      ++i;;
    }
  }
}
