package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class representing a Plugin dependency in a
 * paper-plugin.yml with the attributes of the
 * dependencies
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginDependency {

    /**
     * @return the name of the loaded dependency plugin
     */
    String name();

    /**
     * @return if the plugin should be loaded as a separate instance
     */
    boolean load() default true;

    /**
     * @return if plugin should be disabled if the dependency is
     * not available
     */
    boolean required() default false;

    /**
     * @return if the paper classloader should use the loaded classes
     * from the dependency plugins
     */
    boolean joinClasspath() default false;
}