package ru.supcm.autofactory.util;

/**
 * Lazy Singleton. Initialized if requested (can be cleaned if needed)
 * @param <T> - some class
 */
public final class Singleton<T> {
    private T obj;
    private final Factory<T> factory;
    private final Cleaner<T> cleaner;

    public Singleton(Factory<T> factory) {
        this(factory, null);
    }

    public Singleton(Factory<T> factory, Cleaner<T> cleaner) {
        this(null, factory, cleaner);
    }

    public Singleton(T obj, Factory<T> factory, Cleaner<T> cleaner) {
        this.obj = obj;
        this.factory = factory;
        this.cleaner = cleaner;
    }

    public boolean isPresent() {
        return obj != null;
    }

    public T getOrCreate() {
        if(!isPresent())
            obj = factory.get();
        return obj;
    }

    public void clear() {
        if(cleaner != null)
            cleaner.clean(obj);
        obj = null;
    }

    /**
     * The factory to create an object
     * @param <T>
     */
    public interface Factory<T> {
        T get();
    }

    /**
     * The cleaner that be called BEFORE erasing the object (you may not implement this)
     * Analogue of finalize xD
     * @param <T>
     */
    public interface Cleaner<T> {
        void clean(T obj);
    }
}
