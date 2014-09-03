package io.realm.tests.typed.entities;


import io.realm.ColumnType;
import io.realm.Table;

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

  public static Table initTable(io.realm.ImplicitTransaction transaction) {
    if(!transaction.hasTable("Dog")) {
      Table table = transaction.getTable("Dog");
      index_name  = 0;
      table.addColumn( ColumnType.STRING, "name" );
      return table;
    }
    return transaction.getTable("Dog");
  }

}
