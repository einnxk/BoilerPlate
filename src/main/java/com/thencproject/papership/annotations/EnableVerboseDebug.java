package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that all console output from this plugin,
 * such as infos or debugs are directly broadcasted
 * into the chat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableVerboseDebug {}