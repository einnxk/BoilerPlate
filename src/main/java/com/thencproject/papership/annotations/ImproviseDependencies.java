package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If this annotation is applied to a plugin
 * main class if the mysql-connector or hikariCP
 * is not available at runtime it automatically
 * provides them if the modules are enabled
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ImproviseDependencies {

    /**
     * @return if the Mysql-Connector classes should be provided
     */
    boolean sql() default true;

    /**
     * @return if the HikariCP classes should be provided
     */
    boolean hikari() default true;
}