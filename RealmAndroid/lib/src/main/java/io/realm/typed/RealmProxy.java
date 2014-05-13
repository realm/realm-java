package io.realm.typed;


import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.InvocationHandler;
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


        if(this.rowIndex != -1) {

            String methodName = m.getName();
/*
            if (methodName.startsWith("get")) {

                TableOrView table = this.realm.getDataStore();

                Class<?> type = m.getReturnType();

                String name = methodName.substring(3).toLowerCase();

                if (type.equals(String.class)) {
                    return table.getString(realm.getColumnIndex(name), rowIndex);
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    return ((Long) table.getLong(realm.getColumnIndex(name), rowIndex)).intValue();
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    return table.getLong(realm.getColumnIndex(name), rowIndex);
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    return table.getDouble(realm.getColumnIndex(name), rowIndex);
                } else if (type.equals(float.class) || type.equals(Float.class)) {
                    return table.getFloat(realm.getColumnIndex(name), rowIndex);
                } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    return table.getBoolean(realm.getColumnIndex(name), rowIndex);
                } else if (type.equals(Date.class)) {
                    return table.getDate(realm.getColumnIndex(name), rowIndex);
                }


            } else*/
            if (methodName.startsWith("set")) {


                TableOrView table = this.realm.getDataStore();

                Class<?> type = m.getParameterTypes()[0];

                String name = methodName.substring(3).toLowerCase();

                if (type.equals(String.class)) {
                    table.setString(realm.getColumnIndex(name), rowIndex, (String) args[0]);
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    table.setLong(realm.getColumnIndex(name), rowIndex, (Integer) args[0]);
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    table.setLong(realm.getColumnIndex(name), rowIndex, (Long) args[0]);
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    table.setDouble(realm.getColumnIndex(name), rowIndex, (Double) args[0]);
                } else if (type.equals(float.class) || type.equals(Float.class)) {
                    table.setFloat(realm.getColumnIndex(name), rowIndex, (Float) args[0]);
                } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    table.setBoolean(realm.getColumnIndex(name), rowIndex, (Boolean) args[0]);
                } else if (type.equals(Date.class)) {
                    table.setDate(realm.getColumnIndex(name), rowIndex, (Date) args[0]);
                } else {
                    return null;
                }

                return null;

            }

        }

        return ProxyBuilder.callSuper(proxy, m, args);
    }

}
