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

import io.realm.TableOrView;

class RealmProxy implements InvocationHandler {

    private Realm realm;
    private RealmList realmList;
    private long rowIndex;

    private static Map<String, RealmGetter> getters = new HashMap<String, RealmGetter>();

    public RealmProxy(RealmList realmList, long rowIndex) {
        this.realmList = realmList;
        this.realm = realmList.getRealm();
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
        if(this.realmList != null) {
            return realmList.getTable();
        } else {
            return realm.getTable(classSpec);
        }
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

     //   long rowIndex = ((RealmObject) proxy).rowIndex;

        if(rowIndex != -1) {

            final String methodName = m.getName();

            Class<?> clazz = proxy.getClass().getSuperclass();

            TableOrView table = getTable(clazz);

            if(getters.containsKey(clazz.getSimpleName()+methodName)) {
                return getters.get(clazz.getSimpleName()+methodName).get(table, rowIndex);
            } else {


                if (methodName.startsWith("get")) {

                    Class<?> type = m.getReturnType();

                    String name = methodName.substring(3).toLowerCase();
                    final long columnIndex = table.getColumnIndex(name);

                    if (type.equals(String.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(TableOrView table, long rowIndex) {
                                return table.getString(columnIndex, rowIndex);
                            }
                        });

                        return table.getString(columnIndex, rowIndex);
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(TableOrView table, long rowIndex) {
                                return ((Long) table.getLong(columnIndex, rowIndex)).intValue();
                            }
                        });

                        return ((Long) table.getLong(columnIndex, rowIndex)).intValue();
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
                    } else if (type.equals(byte[].class)) {
                        // Binary
                    } else if (RealmObject.class.equals(type.getSuperclass())) {
                        if(!table.isNullLink(columnIndex, rowIndex)) {
                            return realm.get((Class<? extends RealmObject>) type, table.getLink(columnIndex, rowIndex));
                        } else {
                            return null;
                        }
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

                if (type.equals(String.class)) {
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
                } else if (RealmObject.class.equals(type.getSuperclass())) {
                    // Link
                    RealmObject linkedObject = (RealmObject)args[0];
                    if(linkedObject != null) {
                        if(linkedObject.realmGetRowIndex() == -1) {
                            realm.add(linkedObject);
                        }
                        // Add link
                        table.setLink(table.getColumnIndex(name), rowIndex, linkedObject.realmGetRowIndex());
                    }
                } else {
                    return null;
                }

                return null;

            }

        }

        return ProxyBuilder.callSuper(proxy, m, args);
    }

}
