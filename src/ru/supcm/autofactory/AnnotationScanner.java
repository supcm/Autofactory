package ru.supcm.autofactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class AnnotationScanner {
    private final ClassLoader classLoader;
    private final String basePackage;
    private final boolean useCache;
    private final boolean isJar;
    private final HashMap<Class<? extends Annotation>, List<Class<?>>> cachedClasses;

    /**
     * Scans for annotated classes.
     * @param classLoader - ClassLoader (if needed special; if not, use ClassLoader#getSystemClassLoader)
     * @param basePackage - package to start nested scan from
     * @param useCache - defines caching the results of scan to reuse it (can help with performance due to I/O things in scan)
     */
    public AnnotationScanner(ClassLoader classLoader, String basePackage, boolean useCache) {
        this.classLoader = classLoader;
        this.basePackage = basePackage;
        this.useCache = useCache;
        this.cachedClasses = new HashMap<>();

        isJar = classLoader.getResource(getClass().getName().replace('.', '/') + ".class")
                .getProtocol().equals("jar");
    }

    public AnnotationScanner(String basePackage) {
        this(ClassLoader.getSystemClassLoader(), basePackage, true);
    }

    public AnnotationScanner(String basePackage, boolean useCache) {
        this(ClassLoader.getSystemClassLoader(), basePackage, useCache);
    }

    /**
     * Wrapper to recursive search for annotated classes. Yeah, Imma too stupid to implement it in recursive method, so I need wrapper to do the dirty work.
     * Not the fastest way but safest one
     * @param annotation - annotation class to check on
     * @return list of annotated classes (cached one if useCache is true)
     */
    public List<Class<?>> findClassesWithAnnotation(Class<? extends Annotation> annotation) {
        if(!cachedClasses.containsKey(annotation) || !useCache) {
            List<Class<?>> classes = new ArrayList<>();

            if(isJar) {
                // Jar must have special algorithms
                try {
                    findClassesInJar(annotation, classes);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                InputStream inputStream = classLoader.getResourceAsStream(basePackage.replace(".", "/"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        recursiveModuleFinder(line, "", classes, annotation);
                    }
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if(useCache)
                cachedClasses.put(annotation, new ArrayList<>(classes)); // Putting a copy of list, so it can't be edited in cache
            return classes;
        } else
            return new ArrayList<>(cachedClasses.get(annotation)); // Returning a copy of list, so it can't be edited in cache
    }

    /**
     * Search for class in jar file (uses JarURLConnection).
     * @param annotation - annotation class to check on
     * @param classes - list to add found class
     */
    private void findClassesInJar(Class<? extends Annotation> annotation, List<Class<?>> classes) throws IOException, ClassNotFoundException {
        JarURLConnection connection = (JarURLConnection) classLoader
                .getResource(basePackage.replace(".", "/")).openConnection();

        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();

        for(JarEntry entry = entries.nextElement(); entry != null && entries.hasMoreElements(); entry = entries.nextElement()) {
            String name = entry.getName();
            // Scans all files in jar, so we need to check if it scans in right place of jar
            if(name.startsWith(basePackage.replace(".", "/")) && name.endsWith(".class")) {
                Class<?> clazz = Class.forName(name.replace("/", ".").substring(0, name.length() - 6));
                if (clazz.isAnnotationPresent(annotation))
                    classes.add(clazz);
            }
        }
    }

    /**
     * Recursive search for all classes in package (nested).
     * @param moduleName - a name of class OR package, that was scanned
     * @param packageName - nested package name (empty, if it's root)
     * @param classes - list to add found class
     * @param annotation -
     */
    private void recursiveModuleFinder(String moduleName, String packageName, List<Class<?>> classes,
                                       Class<? extends Annotation> annotation) {
        if(moduleName.lastIndexOf(".") != -1) {
            Class<?> clazz;
            try {
                clazz = Class.forName(basePackage + packageName + "." + moduleName.substring(0, moduleName.lastIndexOf(".")));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if(clazz.isAnnotationPresent(annotation))
                classes.add(clazz);
        } else {
            InputStream inputStream = classLoader.getResourceAsStream((basePackage + packageName).replace(".", "/") + "/" + moduleName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    recursiveModuleFinder(line, packageName + "." + moduleName, classes, annotation);
                }
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
