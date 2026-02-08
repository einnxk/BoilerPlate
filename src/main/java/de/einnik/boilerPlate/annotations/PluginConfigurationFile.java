package de.einnik.boilerPlate.annotations;

import de.einnik.boilerPlate.bind.PluginLoading;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginConfigurationFile {
    String name();
    String version();
    String apiVersion();
    PluginLoading loading() default PluginLoading.STARTUP;
    String author() default "[]";
    String description() default "";
    String website() default "";
    PluginDependency[] dependencies() default {};
}