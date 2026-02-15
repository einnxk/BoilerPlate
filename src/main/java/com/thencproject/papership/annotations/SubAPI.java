package com.thencproject.papership.annotations;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a Sub-API class and simply marks all
 * methods as used
 */
@ApiStatus.Experimental
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubAPI {}