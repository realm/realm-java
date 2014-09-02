package io.realm.tests.typed.entities;

public final class AllColumnsRealmProxy extends AllColumns {

  private static final int rowIndexColumnBoolean = 0;
  public boolean getColumnBoolean() {
    return row.getBoolean( rowIndexColumnBoolean );
  }
  public void setColumnBoolean(boolean value) {
    row.setBoolean( rowIndexColumnBoolean, value );
  }

  private static final int rowIndexColumnDate = 1;
  public java.util.Date getColumnDate() {
    return row.getDate( rowIndexColumnDate );
  }
  public void setColumnDate(java.util.Date value) {
    row.setDate( rowIndexColumnDate, value );
  }

  private static final int rowIndexColumnDouble = 2;
  public double getColumnDouble() {
    return row.getDouble( rowIndexColumnDouble );
  }
  public void setColumnDouble(double value) {
    row.setDouble( rowIndexColumnDouble, value );
  }

  private static final int rowIndexColumnFloat = 3;
  public float getColumnFloat() {
    return row.getFloat( rowIndexColumnFloat );
  }
  public void setColumnFloat(float value) {
    row.setFloat( rowIndexColumnFloat, value );
  }

  private static final int rowIndexColumnLLong = 4;
  public Long getColumnLLong() {
    return row.getLong( rowIndexColumnLLong );
  }
  public void setColumnLLong(Long value) {
    row.setLong( rowIndexColumnLLong, value );
  }

  private static final int rowIndexColumnLong = 5;
  public long getColumnLong() {
    return row.getLong( rowIndexColumnLong );
  }
  public void setColumnLong(long value) {
    row.setLong( rowIndexColumnLong, value );
  }

  private static final int rowIndexColumnString = 6;
  public String getColumnString() {
    return row.getString( rowIndexColumnString );
  }
  public void setColumnString(String value) {
    row.setString( rowIndexColumnString, value );
  }

  private static final int rowIndexIntNumber = 7;
  public int getIntNumber() {
    return (int)row.getLong( rowIndexIntNumber );
  }
  public void setIntNumber(int value) {
    row.setLong( rowIndexIntNumber, value );
  }

  private static final int rowIndexIntegerNumber = 8;
  public Integer getIntegerNumber() {
    return (int)row.getLong( rowIndexIntegerNumber );
  }
  public void setIntegerNumber(Integer value) {
    row.setLong( rowIndexIntegerNumber, value );
  }

}
