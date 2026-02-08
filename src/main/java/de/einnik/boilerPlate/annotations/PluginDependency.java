package de.einnik.boilerPlate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginDependency {
    String name();
    boolean load() default true;
    boolean required() default false;
    boolean joinClasspath() default false;
}