package io.realm.example.entities;

public final class UserRealmProxy extends User {

  private static final int rowIndexEmail = 0;
  public String getEmail() {
    return row.getString( rowIndexEmail );
  }
  public void setEmail(String value) {
    row.setString( rowIndexEmail, value );
  }

  private static final int rowIndexId = 1;
  public int getId() {
    return (int)row.getLong( rowIndexId );
  }
  public void setId(int value) {
    row.setLong( rowIndexId, value );
  }

  private static final int rowIndexName = 2;
  public String getName() {
    return row.getString( rowIndexName );
  }
  public void setName(String value) {
    row.setString( rowIndexName, value );
  }

}
