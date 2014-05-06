package com.realm.typed;

import com.tightdb.Table;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RealmProxy<T> implements InvocationHandler {

    private Table table;
    private long rowIndex;

    public RealmProxy() {

    }


    public RealmProxy(Table table, long rowIndex) {
        this.table = table;
        this.rowIndex = rowIndex;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws InvocationTargetException, IllegalAccessException {



        String methodName = m.getName();

        if (methodName.startsWith("get")) {


            String name = methodName.substring(3).toLowerCase();

            if (m.getReturnType().getName().equals("java.lang.String")) {
                return table.getString(table.getColumnIndex(name), rowIndex);
            } else if (m.getReturnType().equals(Integer.class)) {
                return table.getLong(table.getColumnIndex(name), rowIndex);
            } else if (m.getReturnType().getName().equals("int")) {
                return ((Long) table.getLong(table.getColumnIndex(name), rowIndex)).intValue();
            }


        } else if (methodName.startsWith("set")) {


            String name = methodName.substring(3).toLowerCase();
            if (m.getParameterTypes()[0].getName().equals("java.lang.String")) {
                table.setString(table.getColumnIndex(name), rowIndex, (String) args[0]);
            } else if (m.getParameterTypes()[0].getName().equals("int")) {
                table.setLong(table.getColumnIndex(name), rowIndex, new Long((Integer) args[0]));
            } else {
                System.out.println(m.getParameterTypes()[0].getName());
            }

            return null;
        }

        return m.invoke(proxy, args);
    }


}
