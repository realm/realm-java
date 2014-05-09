package io.realm.typed;


import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import io.realm.TableOrView;

class RealmProxy implements InvocationHandler {

    private RealmList realm;
    private long rowIndex;

    public RealmProxy(RealmList realm, long rowIndex) {
        this.realm = realm;
        this.rowIndex = rowIndex;
    }

    public void realmSetRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {



        String methodName = m.getName();

        if(this.rowIndex != -1) {

            if (methodName.startsWith("get")) {

                TableOrView table = this.realm.getDataStore();

                Class<?> type = m.getReturnType();

                String name = methodName.substring(3).toLowerCase();

                if (type.equals(String.class)) {
                    return table.getString(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(int.class) || m.getReturnType().equals(Integer.class)) {
                    return ((Long) table.getLong(table.getColumnIndex(name), rowIndex)).intValue();
                } else if (type.equals(long.class) || m.getReturnType().equals(Long.class)) {
                    return table.getLong(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(double.class) || m.getReturnType().equals(Double.class)) {
                    return table.getDouble(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(float.class) || m.getReturnType().equals(Float.class)) {
                    return table.getFloat(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(boolean.class) || m.getReturnType().equals(Boolean.class)) {
                    return table.getBoolean(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(Date.class)) {
                    return table.getDate(table.getColumnIndex(name), rowIndex);
                }


            } else if (methodName.startsWith("set")) {

                this.realm.beginWriteTransaction();

                try {

                    TableOrView table = this.realm.getDataStore();

                    Class<?> type = m.getParameterTypes()[0];

                    String name = methodName.substring(3).toLowerCase();

                    if (type.equals(String.class)) {
                        table.setString(table.getColumnIndex(name), rowIndex, (String) args[0]);
                    } else if (type.equals(int.class) || m.getReturnType().equals(Integer.class)) {
                        table.setLong(table.getColumnIndex(name), rowIndex, new Long((Integer) args[0]));
                    } else if (type.equals(long.class) || m.getReturnType().equals(Long.class)) {
                        table.setLong(table.getColumnIndex(name), rowIndex, (Long) args[0]);
                    } else if (type.equals(double.class) || m.getReturnType().equals(Double.class)) {
                        table.setDouble(table.getColumnIndex(name), rowIndex, (Double) args[0]);
                    } else if (type.equals(float.class) || m.getReturnType().equals(Float.class)) {
                        table.setFloat(table.getColumnIndex(name), rowIndex, (Float) args[0]);
                    } else if (type.equals(boolean.class) || m.getReturnType().equals(Boolean.class)) {
                        table.setBoolean(table.getColumnIndex(name), rowIndex, (Boolean) args[0]);
                    } else if (type.equals(Date.class)) {
                        table.setDate(table.getColumnIndex(name), rowIndex, (Date) args[0]);
                    } else {
                        return null;
                    }

                    this.realm.commitWriteTransaction();

                } catch (Throwable t) {
                    t.printStackTrace();
                    this.realm.rollbackWriteTransaction();
                }
                return null;

            }

        }

        return ProxyBuilder.callSuper(proxy, m, args);
    }

}
