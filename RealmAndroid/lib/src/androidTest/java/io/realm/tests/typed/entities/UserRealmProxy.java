package io.realm.tests.typed.entities;


import java.lang.reflect.Field;
public final class UserRealmProxy extends User {

  private static int index_email;
  @Override
  public String getEmail() {
    return row.getString( index_email );
  }
  @Override
  public void setEmail(String value) {
    row.setString( index_email, value );
  }

  private static int index_id;
  @Override
  public int getId() {
    return (int)row.getLong( index_id );
  }
  @Override
  public void setId(int value) {
    row.setLong( index_id, value );
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
    Field[] fields=User.class.getDeclaredFields();
    int i = 0;
    for (Field f : fields) {
      if (f.getName().compareTo("email") == 0) {
        index_email = i;
      }
      if (f.getName().compareTo("id") == 0) {
        index_id = i;
      }
      if (f.getName().compareTo("name") == 0) {
        index_name = i;
      }
      ++i;;
    }
  }
}
