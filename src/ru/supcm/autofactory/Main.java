package ru.supcm.autofactory;

import ru.supcm.autofactory.util.IModuleFactory;
import ru.supcm.autofactory.util.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Main {
    //Some imitation of registry system
    private static final HashMap<String, IModuleFactory<?>> REGISTRY = new HashMap<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Defining the base package, from which the scan will start
        String basePackage = "ru.supcm.autofactory";

        //Search for classes with defined annotations in defined base package
        List<Class<?>> classes = findClassesWithAnnotation(Module.class, basePackage);

        //Registering annotated classes
        for(final Class<?> clazz : classes) {
            REGISTRY.put(clazz.getAnnotation(Module.class).name(),
                    //Some shitty Java 7 Factory with Lazy Singleton (check util package)
                    new IModuleFactory<Object>() {
                @Override
                public Singleton<Object> getInstance() {
                    return new Singleton<>(new Singleton.Factory<Object>() {
                        @Override
                        public Object get() {
                            try {
                                return clazz.getConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            });
        }

        // Just sout to check the results; can be used to do smth in other way
        for(String key : REGISTRY.keySet()) {
            System.out.println(key + ": " + REGISTRY.get(key));
        }
    }

    /**
     * Wrapper to recursive search for annotated classes. Yeah, Imma too stupid to implement it in recursive method, so I need wrapper to do the dirty work.
     * Not the fastest way but safest one
     * TODO: maybe check on annotation in recursive method BEFORE adding it to list???
     * @param annotation - annotation class to check on
     * @param basePackage - package from which scanning will be started
     * @return list of annotated classes
     */
    public static List<Class<?>> findClassesWithAnnotation(Class<? extends Annotation> annotation, String basePackage) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(basePackage.replace(".", "/"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<Class<?>> classes = new ArrayList<>();

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                recursiveModuleFinder(line, basePackage, "", classes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); ) {
            if(!iterator.next().isAnnotationPresent(annotation))
                iterator.remove();
        }

        return classes;
    }

    /**
     * Recursive search for all classes in package (nested); TODO: maybe check on annotation there BEFORE adding it to list???
     * @param moduleName - a name of class OR package, that was scanned
     * @param basePackage - package from which scanning started
     * @param packageName - nested package name (empty, if it's root)
     * @param classes - list to add found class
     */
    private static void recursiveModuleFinder(String moduleName, String basePackage, String packageName, List<Class<?>> classes) {
        if(moduleName.lastIndexOf(".") != -1) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(basePackage + packageName + "." + moduleName.substring(0, moduleName.lastIndexOf(".")));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            classes.add(clazz);
        } else {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream((basePackage + packageName).replace(".", "/") + "/" + moduleName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    recursiveModuleFinder(line, basePackage, packageName + "." + moduleName, classes);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
