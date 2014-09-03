package io.realm.tests.typed.entities;


import java.lang.reflect.Field;
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

  static {
    Field[] fields=AllColumns.class.getDeclaredFields();
    int i = 0;
    for (Field f : fields) {
      if (f.getName().compareTo("columnBoolean") == 0) {
        index_columnBoolean = i;
      }
      if (f.getName().compareTo("columnDate") == 0) {
        index_columnDate = i;
      }
      if (f.getName().compareTo("columnDouble") == 0) {
        index_columnDouble = i;
      }
      if (f.getName().compareTo("columnFloat") == 0) {
        index_columnFloat = i;
      }
      if (f.getName().compareTo("columnLLong") == 0) {
        index_columnLLong = i;
      }
      if (f.getName().compareTo("columnLong") == 0) {
        index_columnLong = i;
      }
      if (f.getName().compareTo("columnString") == 0) {
        index_columnString = i;
      }
      if (f.getName().compareTo("intNumber") == 0) {
        index_intNumber = i;
      }
      if (f.getName().compareTo("integerNumber") == 0) {
        index_integerNumber = i;
      }
      ++i;;
    }
  }
}
