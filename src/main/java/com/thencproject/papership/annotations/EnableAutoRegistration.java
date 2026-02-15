package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for the Main plugin class to mark
 * it as a plugin that scans packages to register
 * classes marked with @AutoListener or with @AutoCommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableAutoRegistration {

    /**
     * specify the packages in which there is searched
     * for auto registration
     * gives a small compile time performance boost
     * @return an array of the specified packages
     */
    String[] packages() default {};
}