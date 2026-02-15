package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that errors with the level severe are
 * broadcasted into the chat,
 * only errors from this plugin are broadcasted
 * to the chat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableDebug {}