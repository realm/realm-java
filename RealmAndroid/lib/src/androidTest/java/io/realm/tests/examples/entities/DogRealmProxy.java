package io.realm.tests.examples.entities;

public final class DogRealmProxy extends Dog {

  private static final int rowIndexAge = 0;
  public int getAge() {
    return (int)row.getLong( rowIndexAge );
  }
  public void setAge(int value) {
    row.setLong( rowIndexAge, value );
  }

  private static final int rowIndexName = 1;
  public String getName() {
    return row.getString( rowIndexName );
  }
  public void setName(String value) {
    row.setString( rowIndexName, value );
  }

}
