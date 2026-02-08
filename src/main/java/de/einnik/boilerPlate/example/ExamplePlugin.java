package de.einnik.boilerPlate.example;

import de.einnik.boilerPlate.annotations.*;
import de.einnik.boilerPlate.bind.BoilerPlateProvider;
import de.einnik.boilerPlate.bind.PluginLoading;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@BoilerPlatePlugin
@EnableAutoRegistration
@EnableDependencyImprovisation(hikari = false, sql = false)
@EnableDebug
@EnableVerboseDebug
@PluginConfigurationFile(
        name = "ExamplePlugin",
        version = "1.0",
        apiVersion = "1.21",
        loading = PluginLoading.POSTWORLD,
        author = "[ EinNik ]",
        description = "Plugin to reduce BoilerPlate code in your Plugins",
        website = "https://github.com/einnxk/BoilderPlate",
        dependencies = {
                @PluginDependency(
                        name = "LuckPerms",
                        load = true,
                        required = true,
                        joinClasspath = true
                ),
                @PluginDependency(
                        name = "Essentials",
                        load = true,
                        required = true,
                        joinClasspath = true
                )
        }
)
public final class ExamplePlugin extends JavaPlugin {

    @AutoProvideFile(copyIfNotExists = false)
    private final File file = new File("plugin.yml");

    @Override
    public void onLoad(){
        BoilerPlateProvider.initialize(this);
    }
}