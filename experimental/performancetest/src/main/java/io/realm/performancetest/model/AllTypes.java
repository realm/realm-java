package io.realm.performancetest.model;

import java.util.Date;

/**
 * Created by cm on 13/11/14.
 */
public interface AllTypes {
    String getColumnString();

    void setColumnString(String columnString);

    long getColumnLong();

    void setColumnLong(long columnLong);

    float getColumnFloat();

    void setColumnFloat(float columnFloat);

    double getColumnDouble();

    void setColumnDouble(double columnDouble);

    boolean isColumnBoolean();

    void setColumnBoolean(boolean columnBoolean);

    Date getColumnDate();

    void setColumnDate(Date columnDate);

    byte[] getColumnBinary();

    void setColumnBinary(byte[] columnBinary);

    Dog getColumnRealmObject();

    void setColumnRealmObject(Dog columnRealmObject);
}
