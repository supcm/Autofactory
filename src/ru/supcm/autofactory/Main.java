package ru.supcm.autofactory;

import ru.supcm.autofactory.util.IModuleFactory;
import ru.supcm.autofactory.util.Singleton;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class Main {
    //Some imitation of registry system
    private static final HashMap<String, IModuleFactory<?>> REGISTRY = new HashMap<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AnnotationScanner finder = new AnnotationScanner("ru.supcm.autofactory");
        //Search for classes with defined annotations in defined base package
        List<Class<?>> classes = finder.findClassesWithAnnotation(Module.class);

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

        System.out.println('\n');

        // Check for cache with debugger
        for(Class<?> clazz : finder.findClassesWithAnnotation(Module.class)) {
            System.out.println(clazz.getName());
        }
    }
}
