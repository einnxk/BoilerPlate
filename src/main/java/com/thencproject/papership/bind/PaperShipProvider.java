package com.thencproject.papership.bind;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central API class where a plugin can be registered
 * as a papership plugin to reduce boilerplate code
 */
@SuppressWarnings("unused")
public class PaperShipProvider {

    /**
     * Registers a normal paper plugin as a papership plugin
     * which enables all the features and loads the bootstrap
     * processing of that plugin
     * @param pluginClass the plugin that is registered
     */
    public static void initialize(JavaPlugin pluginClass) {
        Bootstrap.initialize(pluginClass);
    }
}