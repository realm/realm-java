/*
package io.realm;


import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.realm.internal.LinkView;
import io.realm.internal.Row;

class RealmProxy implements InvocationHandler {

    private Realm realm = null;
    private Row row = null;

    private static Map<String, RealmGetter> getters = new HashMap<String, RealmGetter>();

    public RealmProxy(Realm realm, Row row) {
        this.realm = realm;
        this.row = row;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

        if(row != null) {

            final String methodName = m.getName();

            Class<?> clazz = proxy.getClass().getSuperclass();

            if(getters.containsKey(clazz.getSimpleName()+methodName)) {
                return getters.get(clazz.getSimpleName()+methodName).get(row);
            } else {

                if (methodName.startsWith("get")) {

                    Class<?> type = m.getReturnType();

                    String name = methodName.substring(3).toLowerCase(Locale.getDefault());
                    final long columnIndex = row.getColumnIndex(name);

                    if (type.equals(String.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(Row row) {
                                return row.getString(columnIndex);
                            }
                        });

                        return row.getString(columnIndex);
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {

                        getters.put(clazz.getSimpleName()+methodName, new RealmGetter() {
                            @Override
                            public Object get(Row row) {
                                return ((Long) row.getLong(columnIndex)).intValue();
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

                        if(!row.isNullLink(columnIndex)) {
                            return realm.get((Class<? extends RealmObject>) type, row.getLink(columnIndex));
                        } else {
                            return null;
                        }

                    } else if (RealmList.class.isAssignableFrom(type)) {
                        // Link List
                        Field f = m.getDeclaringClass().getDeclaredField(name);
                        Type genericType = f.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType pType = (ParameterizedType) genericType;
                            Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                            if(RealmObject.class.equals(actual.getSuperclass())) {
                                return new RealmLinkList(actual, row.getLinkList(columnIndex), realm);
                            }

                        }
                    }


                }
            }

            if (methodName.startsWith("set")) {

                Class<?> type = m.getParameterTypes()[0];

                String name = methodName.substring(3).toLowerCase(Locale.getDefault());
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

                    if(linkedObject != null) {
                        if(linkedObject.realmGetRow() == null) {
                            realm.add(linkedObject);
                            row.setLink(columnIndex, linkedObject.realmAddedAtRowIndex);
                        } else {
                            row.setLink(columnIndex, linkedObject.realmGetRow().getIndex());
                        }

                    } else {
                        row.nullifyLink(columnIndex);
                    }

                } else if (RealmList.class.isAssignableFrom(type)) {
                    // Link List
                    Field f = m.getDeclaringClass().getDeclaredField(name);
                    Type genericType = f.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                        if(RealmObject.class.equals(actual.getSuperclass())) {

                            LinkView links = row.getLinkList(columnIndex);

                            // Loop through list and add them to the link list and possibly to the realm
                            for(RealmObject linkedObject : (RealmList<RealmObject>)args[0]) {

                                if(linkedObject.realmGetRow() == null) {
                                    if(linkedObject.realmAddedAtRowIndex == -1) {
                                        realm.add(linkedObject);
                                    }
                                    links.add(linkedObject.realmAddedAtRowIndex);
                                } else {
                                    links.add(linkedObject.realmGetRow().getIndex());
                                }
                            }
                        }

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
*/
