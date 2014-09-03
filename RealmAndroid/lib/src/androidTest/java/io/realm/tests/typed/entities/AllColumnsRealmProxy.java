package io.realm.tests.typed.entities;


import io.realm.ColumnType;
import io.realm.Table;

public final class AllColumnsRealmProxy extends AllColumns {

  private static int index_columnBoolean;
  @Override
  public boolean getColumnBoolean() {
    return row.getBoolean( index_columnBoolean );
  }
  @Override
  public void setColumnBoolean(boolean value) {
    row.setBoolean( index_columnBoolean, value );
  }

  private static int index_columnDate;
  @Override
  public java.util.Date getColumnDate() {
    return row.getDate( index_columnDate );
  }
  @Override
  public void setColumnDate(java.util.Date value) {
    row.setDate( index_columnDate, value );
  }

  private static int index_columnDouble;
  @Override
  public double getColumnDouble() {
    return row.getDouble( index_columnDouble );
  }
  @Override
  public void setColumnDouble(double value) {
    row.setDouble( index_columnDouble, value );
  }

  private static int index_columnFloat;
  @Override
  public float getColumnFloat() {
    return row.getFloat( index_columnFloat );
  }
  @Override
  public void setColumnFloat(float value) {
    row.setFloat( index_columnFloat, value );
  }

  private static int index_columnLLong;
  @Override
  public Long getColumnLLong() {
    return row.getLong( index_columnLLong );
  }
  @Override
  public void setColumnLLong(Long value) {
    row.setLong( index_columnLLong, value );
  }

  private static int index_columnLong;
  @Override
  public long getColumnLong() {
    return row.getLong( index_columnLong );
  }
  @Override
  public void setColumnLong(long value) {
    row.setLong( index_columnLong, value );
  }

  private static int index_columnString;
  @Override
  public String getColumnString() {
    return row.getString( index_columnString );
  }
  @Override
  public void setColumnString(String value) {
    row.setString( index_columnString, value );
  }

  private static int index_intNumber;
  @Override
  public int getIntNumber() {
    return (int)row.getLong( index_intNumber );
  }
  @Override
  public void setIntNumber(int value) {
    row.setLong( index_intNumber, value );
  }

  private static int index_integerNumber;
  @Override
  public Integer getIntegerNumber() {
    return (int)row.getLong( index_integerNumber );
  }
  @Override
  public void setIntegerNumber(Integer value) {
    row.setLong( index_integerNumber, value );
  }

  public static Table initTable(io.realm.ImplicitTransaction transaction) {
    if(!transaction.hasTable("AllColumns")) {
      Table table = transaction.getTable("AllColumns");
      index_columnBoolean  = 0;
      table.addColumn( ColumnType.BOOLEAN, "columnboolean" );
      index_columnDate  = 1;
      table.addColumn( ColumnType.DATE, "columndate" );
      index_columnDouble  = 2;
      table.addColumn( ColumnType.DOUBLE, "columndouble" );
      index_columnFloat  = 3;
      table.addColumn( ColumnType.FLOAT, "columnfloat" );
      index_columnLLong  = 4;
      table.addColumn( ColumnType.INTEGER, "columnllong" );
      index_columnLong  = 5;
      table.addColumn( ColumnType.INTEGER, "columnlong" );
      index_columnString  = 6;
      table.addColumn( ColumnType.STRING, "columnstring" );
      index_intNumber  = 7;
      table.addColumn( ColumnType.INTEGER, "intnumber" );
      index_integerNumber  = 8;
      table.addColumn( ColumnType.INTEGER, "integernumber" );
      return table;
    }
    return transaction.getTable("AllColumns");
  }

}
