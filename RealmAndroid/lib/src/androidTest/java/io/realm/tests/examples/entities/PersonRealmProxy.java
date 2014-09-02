package io.realm.tests.examples.entities;

public final class PersonRealmProxy extends Person {

  private static final int rowIndexName = 0;
  public String getName() {
    return row.getString( rowIndexName );
  }
  public void setName(String value) {
    row.setString( rowIndexName, value );
  }

}
