package ru.supcm.autofactory.util;

import java.lang.reflect.InvocationTargetException;

/***
 * Factory used to create any module; used to register module for availability
 * @param <T> module class
 */
public interface IModuleFactory<T> {
    /***
     * Uses instance of singleton of a module. MUST return an existing instance of singleton.
     * @return lazy-singleton of a module
     */
    Singleton<T> getInstance() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
