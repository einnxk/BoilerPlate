package com.thencproject.papership.annotations;

import com.thencproject.papership.bind.AutoListenerPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoListener {
    AutoListenerPriority priority() default AutoListenerPriority.Normal;
}