package io.realm.tests.examples.entities;


import io.realm.ColumnType;
import io.realm.Table;

public final class PersonRealmProxy extends Person {

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
    if(!transaction.hasTable("Person")) {
      Table table = transaction.getTable("Person");
      index_name  = 0;
      table.addColumn( ColumnType.STRING, "name" );
      return table;
    }
    return transaction.getTable("Person");
  }

}
