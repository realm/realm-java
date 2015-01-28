package io.realm.internal;

import java.io.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;

public class RealmProxyMediatorAdapter implements RealmProxyMediator {

    private final Map<Class<? extends RealmObject>, RealmProxyMediator> mediators;

    /**
     * Creates a proxy mediator adapter that loads all available proxy mediators and determine
     * which mediator handles which mapping.
     *
     * If two mediators handle the same file a random mediator is used.
     */
    public RealmProxyMediatorAdapter() {
        mediators = new HashMap<Class<? extends RealmObject>, RealmProxyMediator>();
        try {
            List<String> files = findMediatorFiles();
            for (String file : files) {
                try {
                    Class<?> clazz = Class.forName(file);
                    Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    RealmProxyMediator mediator = (RealmProxyMediator) constructor.newInstance();
                    List<Class<? extends RealmObject>> realmClasses = mediator.getModelClasses();
                    for (Class<? extends RealmObject> realmClass : realmClasses) {
                        mediators.put(realmClass, mediator);
                    }

                } catch (IndexOutOfBoundsException e) {
                    throw new RealmException("Could not find a a constructor in RealmProxyMediatorImpl class: " + file + ". " + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
                } catch (ClassNotFoundException e) {
                    throw new RealmException("Could not find the generated RealmProxyMediatorImpl class.: " + file + ". " +  RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE);
                } catch (InvocationTargetException e) {
                    throw new RealmException("Could not initialize RealmProxyMediatorImpl: " + file, e);
                } catch (InstantiationException e) {
                    throw new RealmException("Could not initialize RealmProxyMediatorImpl: " + file, e);
                } catch (IllegalAccessException e) {
                    throw new RealmException("Could not initialize RealmProxyMediatorImpl: " + file, e);
                }
            }

        } catch (IOException e) {
            throw new RealmException("Could not find or read realm.properties files", e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Find the names of all available RealmProxyMediatorImpl_<UUID> files
    private List<String> findMediatorFiles() throws java.io.IOException, URISyntaxException {
        List<String> list = new ArrayList<String>();

        Enumeration<URL> systemResources = Realm.class.getClassLoader().getResources("io/realm/realm.properties");
        while (systemResources.hasMoreElements()) {
            InputStream in = systemResources.nextElement().openStream();
            Properties props = new Properties();
            props.load(in);
            in.close();
            list.add(props.getProperty("proxymediator"));
        }
        return list;
    }

    @Override
    public Table createTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        return null;
    }

    @Override
    public void validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {

    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmObject> clazz) {
        return null;
    }

    @Override
    public String getClassModelName(Class<? extends RealmObject> clazz) {
        return null;
    }

    @Override
    public <E extends RealmObject> E newInstance(Class<E> clazz) {
        return null;
    }

    @Override
    public List<Class<? extends RealmObject>> getModelClasses() {
        return null;
    }

    @Override
    public <E extends RealmObject> E copyToRealm(Realm realm, E object) {
        return null;
    }
}
