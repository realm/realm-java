package io.realm.typed;


import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Row;
import io.realm.TableOrView;

class RealmProxy implements InvocationHandler {

    private Row row = null;

    private static Map<String, RealmGetter> getters = new HashMap<String, RealmGetter>();

    public RealmProxy(Row row) {
        this.row = row;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

     //   long rowIndex = ((RealmObject) proxy).rowIndex;

        if(row != null) {

            final String methodName = m.getName();

            Class<?> clazz = proxy.getClass().getSuperclass();


            if(getters.containsKey(clazz.getSimpleName()+methodName)) {
                return getters.get(clazz.getSimpleName()+methodName).get(row);
            } else {


                if (methodName.startsWith("get")) {

                    Class<?> type = m.getReturnType();

                    String name = methodName.substring(3).toLowerCase();
                    final long columnIndex = row.getColumnIndex(name);

                    if (type.equals(String.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(TableOrView table, long rowIndex) {
                                return table.getString(columnIndex, rowIndex);
                            }
                        });

                        return row.getString(columnIndex);
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(TableOrView table, long rowIndex) {
                                return ((Long) table.getLong(columnIndex, rowIndex)).intValue();
                            }
                        });

                        return ((Long) row.getLong(columnIndex)).intValue();
                    } else if (type.equals(long.class) || type.equals(Long.class)) {
                        return row.getLong(columnIndex);
                    } else if (type.equals(double.class) || type.equals(Double.class)) {
                        return row.getDouble(columnIndex);
                    } else if (type.equals(float.class) || type.equals(Float.class)) {
                        return row.getFloat(columnIndex);
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        return row.getBoolean(columnIndex);
                    } else if (type.equals(Date.class)) {
                        return row.getDate(columnIndex);
                    } else if (type.equals(byte[].class)) {
                        // Binary
                    } else if (RealmObject.class.equals(type.getSuperclass())) {
                        /*
                        if(!row.isNullLink(columnIndex)) {
                            return realm.get((Class<? extends RealmObject>) type, row.getLink(columnIndex));
                        } else {
                            return null;
                        }
                        */
                    } else if (RealmList.class.equals(type)) {
                        Field f = m.getDeclaringClass().getDeclaredField(name);
                        Type genericType = f.getGenericType();
                        System.out.println(genericType);
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType pType = (ParameterizedType) genericType;
                            Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                            System.out.println(actual.getName() + " - " + RealmObject.class.equals(actual.getSuperclass()));
                        }
                    /*
                    *
                    * List of links, should just return a new RealmList with a view set to represent the links
                    * int columnIndex = table.getColumnIndex(name);
                    *
                    * TableOrView t = table.getLinks(columnIndex);
                    *
                    * return new RealmList<?>
                    *
                    *
                    * */
                    }


                }
            }

            if (methodName.startsWith("set")) {

                Class<?> type = m.getParameterTypes()[0];

                String name = methodName.substring(3).toLowerCase();
                final long columnIndex = row.getColumnIndex(name);

                if (type.equals(String.class)) {
                    row.setString(columnIndex, (String) args[0]);
                } else if (type.equals(int.class) || type.equals(Integer.class)) {
                    row.setLong(columnIndex, (Integer) args[0]);
                } else if (type.equals(long.class) || type.equals(Long.class)) {
                    row.setLong(columnIndex, (Long) args[0]);
                } else if (type.equals(double.class) || type.equals(Double.class)) {
                    row.setDouble(columnIndex, (Double) args[0]);
                } else if (type.equals(float.class) || type.equals(Float.class)) {
                    row.setFloat(columnIndex, (Float) args[0]);
                } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    row.setBoolean(columnIndex, (Boolean) args[0]);
                } else if (type.equals(Date.class)) {
                    row.setDate(columnIndex, (Date) args[0]);
                } else if (RealmObject.class.equals(type.getSuperclass())) {
                    // Link
                    RealmObject linkedObject = (RealmObject)args[0];
                    /*
                    if(linkedObject != null) {
                        if(linkedObject.realmGetRowIndex() == -1) {
                            realm.add(linkedObject);
                        }
                        // Add link
                        row.setLink(columnIndex, linkedObject.realmGetRowIndex());
                    } else {
                        row.nullifyLink(columnIndex);
                    }
                    */
                } else {
                    return null;
                }

                return null;

            }

        }

        return ProxyBuilder.callSuper(proxy, m, args);
    }

}
