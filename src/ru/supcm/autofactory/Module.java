package ru.supcm.autofactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * To use the annotation in registry things you must do it in runtime;
 * Pay attention to check annotation in init or before the main work ('cause of performance issues due to I/O things)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {
    String name();
}
