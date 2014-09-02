package io.realm.tests.typed.entities;

public final class DogRealmProxy extends Dog {

  private static final int rowIndexName = 0;
  public String getName() {
    return row.getString( rowIndexName );
  }
  public void setName(String value) {
    row.setString( rowIndexName, value );
  }

}
