package com.thencproject.papership.annotations;

import com.thencproject.papership.enums.PluginLoading;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Central class representing a plugin configuration
 * file with all it attributes defined by these
 * annotations parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PluginConfigurationFile {

    /**
     * @return the name of the plugin
     */
    String name();

    /**
     * @return the version of the plugin
     */
    String version();

    /**
     * @return version of the used paper api
     */
    String apiVersion();

    /**
     * When should the plugin be loaded when the server is started
     * or after the World is loaded
     * @return when the plugin is loaded
     */
    PluginLoading loading() default PluginLoading.STARTUP;

    /**
     * The Autors of the Plugin without []
     * @return the authors of the plugin
     */
    String author() default "[]";

    /**
     * @return a short description of the Plugin
     */
    String description() default "";

    /**
     * @return the website of the plugin or the makers
     */
    String website() default "";

    /**
     * The Plugin Dependencies if their paper-plugin.yml
     * attributes defined here
     * @return an array of the Plugin dependencies
     */
    PluginDependency[] dependencies() default {};
}