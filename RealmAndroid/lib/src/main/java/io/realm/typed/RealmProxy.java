package io.realm.typed;


import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

import io.realm.TableOrView;

class RealmProxy implements InvocationHandler {

    private Realm realm = null;
    private RealmList realmList;
    private long rowIndex;

    public RealmProxy(RealmList realmList, long rowIndex) {
        this.realmList = realmList;
        this.rowIndex = rowIndex;
    }

    public RealmProxy(Realm realm, long rowIndex) {
        this.realm = realm;
        this.rowIndex = rowIndex;
    }

    public void realmSetRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    private TableOrView getTable(Class<?> classSpec) {
        if(this.realm == null) {
            return realmList.getTable();
        } else {
            return realm.getTable(classSpec);
        }
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

        System.out.println("p " + proxy.getClass().getSuperclass());

        if(this.rowIndex != -1) {

            String methodName = m.getName();

            TableOrView table = getTable(proxy.getClass().getSuperclass());

            if (methodName.startsWith("get")) {

                Class<?> type = m.getReturnType();

                String name = methodName.substring(3).toLowerCase();

                if (type.equals(String.class)) {
                    return table.getString(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    return ((Long) table.getLong(table.getColumnIndex(name), rowIndex)).intValue();
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    return table.getLong(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    return table.getDouble(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(float.class) || type.equals(Float.class)) {
                    return table.getFloat(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    return table.getBoolean(table.getColumnIndex(name), rowIndex);
                } else if (type.equals(Date.class)) {
                    return table.getDate(table.getColumnIndex(name), rowIndex);
                }


            } else
            if (methodName.startsWith("set")) {

                Class<?> type = m.getParameterTypes()[0];

                String name = methodName.substring(3).toLowerCase();

                if (type.equals(String.class)) {
                    System.out.println((String) args[0]);
                    table.setString(table.getColumnIndex(name), rowIndex, (String) args[0]);
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    table.setLong(table.getColumnIndex(name), rowIndex, (Integer) args[0]);
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    table.setLong(table.getColumnIndex(name), rowIndex, (Long) args[0]);
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    table.setDouble(table.getColumnIndex(name), rowIndex, (Double) args[0]);
                } else if (type.equals(float.class) || type.equals(Float.class)) {
                    table.setFloat(table.getColumnIndex(name), rowIndex, (Float) args[0]);
                } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    table.setBoolean(table.getColumnIndex(name), rowIndex, (Boolean) args[0]);
                } else if (type.equals(Date.class)) {
                    table.setDate(table.getColumnIndex(name), rowIndex, (Date) args[0]);
                } else {
                    return null;
                }

                return null;

            }

        }

        return ProxyBuilder.callSuper(proxy, m, args);
    }

}
