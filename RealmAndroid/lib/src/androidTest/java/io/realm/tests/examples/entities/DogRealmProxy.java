package io.realm.tests.examples.entities;


import io.realm.ColumnType;
import io.realm.Table;

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

  public static Table initTable(io.realm.ImplicitTransaction transaction) {
    if(!transaction.hasTable("Dog")) {
      Table table = transaction.getTable("Dog");
      index_age  = 0;
      table.addColumn( ColumnType.INTEGER, "age" );
      index_name  = 1;
      table.addColumn( ColumnType.STRING, "name" );
      return table;
    }
    return transaction.getTable("Dog");
  }

}
