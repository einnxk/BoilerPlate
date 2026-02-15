package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields that are marked with this annotation will
 * be tried to get from the server plugin directory if
 * not available and if copyIfNotExists Field is true
 * then a File is copied from the resource folder if
 * there is one that matches the Filename
 * After the instance is loaded the file directs to the
 * absolute path of the file on the server
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoWiredFile {

    /**
     * Copies the file from the resource folder if it
     * is not present in the plugin directory
     * @return if the file should be copied from resources
     */
    boolean copyIfNotExists() default true;
}