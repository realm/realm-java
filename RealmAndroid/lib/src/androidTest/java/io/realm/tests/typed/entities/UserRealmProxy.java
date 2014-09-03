package io.realm.tests.typed.entities;


import io.realm.ColumnType;
import io.realm.Table;

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

  public static Table initTable(io.realm.ImplicitTransaction transaction) {
    if(!transaction.hasTable("User")) {
      Table table = transaction.getTable("User");
      index_email  = 0;
      table.addColumn( ColumnType.STRING, "email" );
      index_id  = 1;
      table.addColumn( ColumnType.INTEGER, "id" );
      index_name  = 2;
      table.addColumn( ColumnType.STRING, "name" );
      return table;
    }
    return transaction.getTable("User");
  }

}
