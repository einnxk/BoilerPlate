package de.einnik.boilerPlate.bind;

import org.bukkit.plugin.java.JavaPlugin;

public class BoilerPlateProvider {

    public static void initialize(JavaPlugin pluginClass) {
        BoilerPlateBootstrap.initialize(pluginClass);
    }

    public static void shutdown(JavaPlugin pluginClass) {
        BoilerPlateBootstrap.shutdown(pluginClass);
    }
}