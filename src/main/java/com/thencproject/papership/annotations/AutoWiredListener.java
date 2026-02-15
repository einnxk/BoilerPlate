package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this annotation are if enabled
 * in the main class automatically registered als
 * Bukkit Listener which means they have to implement
 * the Bukkit Listener
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoWiredListener {}