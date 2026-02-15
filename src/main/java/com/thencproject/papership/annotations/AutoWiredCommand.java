package com.thencproject.papership.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this annotation are if enabled
 * in the main class automatically registered als
 * Paper Commands which means they have to extend
 * the abstract Paper Command class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoWiredCommand {

    /**
     * @return the commands string
     */
    String command();

    /**
     * @return the fallback commands string
     */
    String fallbackPrefix() default "";

    /**
     * If not set the command does not need a permission
     * @return the permission needed to execute the command
     */
    String permission() default "";

    /**
     * @return a short description of the command
     */
    String description() default "";

    /**
     * @return an array of command shortcuts
     */
    String[] aliases() default {};
}