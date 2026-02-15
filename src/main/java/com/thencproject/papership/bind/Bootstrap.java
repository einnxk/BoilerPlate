package com.thencproject.papership.bind;

import com.google.common.reflect.ClassPath;
import com.thencproject.papership.annotations.*;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * Processing class for the main bootstrap class
 * that is handling further processes -
 * Listeners and Commands are registered here
 */
public class Bootstrap {

    /**
     * Register a plugin as a papership plugin and
     * calls the further methods here
     * @param plugin the plugin that is registered
     */
    public static void initialize(JavaPlugin plugin) {
        Class<?> pluginClass = plugin.getClass();

        if (!pluginClass.isAnnotationPresent(PaperPlugin.class)) {
            throw new IllegalStateException("Plugin class must be annotated with @PaperPlugin");
        }

        boolean debugEnabled = pluginClass.isAnnotationPresent(EnableDebug.class);
        boolean verboseDebugEnabled = pluginClass.isAnnotationPresent(EnableVerboseDebug.class);
        PaperShipLogger bpLogger = new PaperShipLogger(plugin, debugEnabled, verboseDebugEnabled);

        injectLogger(plugin, bpLogger);

        if (pluginClass.isAnnotationPresent(ImproviseDependencies.class)) {
            provideDependencies(plugin, pluginClass);
        }

        provideFiles(plugin, pluginClass);

        validateDependencies(pluginClass);

        if (pluginClass.isAnnotationPresent(EnableAutoRegistration.class)) {
            autoRegister(plugin, pluginClass);
        }
    }

    /**
     * Helper Method that calls the File Provider for providing
     * fields marked with the fetching annotation
     * @param plugin plugin called by
     * @param pluginClass class of that plugin
     */
    private static void provideFiles(JavaPlugin plugin, Class<?> pluginClass) {
        String packageName = pluginClass.getPackageName();
        FileProvider.provideFiles(plugin, packageName);
    }

    /**
     * Helper Method that calls and then download the SQL connectors
     * and HikariCP
     * @param plugin plugin called by
     * @param pluginClass class of that plugin
     */
    private static void provideDependencies(JavaPlugin plugin, Class<?> pluginClass) {
        ImproviseDependencies annotation = pluginClass.getAnnotation(ImproviseDependencies.class);

        plugin.getLogger().fine("Dependency improvisation enabled");

        DependencyProvider.provideDependencies(
                plugin,
                annotation.sql(),
                annotation.hikari()
        );
    }

    /**
     * Helper Method to initialize the verbose logger
     * @param plugin plugin called by
     * @param customLogger an instance of a PaperShipLogger
     */
    private static void injectLogger(JavaPlugin plugin, PaperShipLogger customLogger) {
        try {
            Field loggerField = JavaPlugin.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(plugin, customLogger);

        } catch (Exception e) {
            throw new ParentLoggerInitializeException(e);
        }
    }

    /**
     * helper method to check if the dependencies are present
     * @param pluginClass main class of the plugin
     */
    private static void validateDependencies(Class<?> pluginClass) {
        PluginConfigurationFile config = pluginClass.getAnnotation(PluginConfigurationFile.class);
        if (config == null) {
            return;
        }

        for (PluginDependency dep : config.dependencies()) {
            Plugin dependencyPlugin = Bukkit.getPluginManager().getPlugin(dep.name());

            if (dependencyPlugin == null) {
                if (dep.required()) {
                    throw new DependencyNotFoundException("Required dependency '" + dep.name() + "' not found!");
                }
            }
        }
    }

    /**
     * Search for auto listeners and commands in the given packages or
     * if not defined all
     * @param plugin the plugin
     * @param pluginClass the main class of the plugin
     */
    private static void autoRegister(JavaPlugin plugin, Class<?> pluginClass) {
        EnableAutoRegistration config = pluginClass.getAnnotation(EnableAutoRegistration.class);
        String[] packages = config.packages();

        if (packages.length == 0) {
            packages = new String[]{pluginClass.getPackageName()};
        }

        for (String packageName : packages) {
            scanAndRegister(plugin, packageName);
        }
    }

    /**
     * Search for auto listeners and commands in the given packages or
     * if not defined all and automatically registers them
     * @param plugin the plugin
     * @param packageName the absolute package
     */
    private static void scanAndRegister(JavaPlugin plugin, String packageName) {
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            Set<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive(packageName);

            for (ClassPath.ClassInfo classInfo : classes) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());

                    if (clazz.isAnnotationPresent(AutoWiredListener.class)) {
                        registerListener(plugin, clazz);
                    }

                    if (clazz.isAnnotationPresent(AutoWiredCommand.class)) {
                        registerCommand(plugin, clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not load class: " + classInfo.getName());
                }
            }

        } catch (Exception e) {
            throw new InternalBootstrapException("Failed to register auto-registered listeners", e);
        }
    }

    /**
     * Register a Listener to the Bukkit Event Manager
     * @param plugin the plugin the listener should be registered to
     * @param clazz the class annotated as a listener
     */
    private static void registerListener(JavaPlugin plugin, Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) instance, plugin);
            }
        } catch (Exception e) {
            throw new AutoRegistrationException("Error while instantiating listener " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Register a command to paper commandMap by a class that is
     * annotated with AutoWiredCommand
     * @param plugin the plugin the command is from
     * @param clazz the class that is annotated as a command
     */
    private static void registerCommand(JavaPlugin plugin, Class<?> clazz) {
        try {
            AutoWiredCommand annotation = clazz.getAnnotation(AutoWiredCommand.class);

            Command commandInstance = getCommand(clazz, annotation);

            CommandMap commandMap = getCommandMap();
            String fallbackPrefix = annotation.fallbackPrefix().isEmpty()
                    ? plugin.getName().toLowerCase()
                    : annotation.fallbackPrefix();

            commandMap.register(fallbackPrefix, commandInstance);

        } catch (NoSuchMethodException e) {
            throw new AutoRegistrationException(
                    "Command class " + clazz.getSimpleName() +
                            " must have a constructor with String parameter", e
            );
        } catch (Exception e) {
            throw new AutoRegistrationException(
                    "Error while instantiating command " + clazz.getSimpleName(), e
            );
        }
    }

    /**
     * @param clazz the class that is annotated as a command
     * @param annotation the annotation that specifies the command
     * @return a Bukkit Command
     */
    private static @NonNull Command getCommand(Class<?> clazz, AutoWiredCommand annotation) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (!Command.class.isAssignableFrom(clazz)) {
            throw new AutoRegistrationException(
                    "Class " + clazz.getSimpleName() + " must extend Command to use @AutoCommand"
            );
        }

        Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        Command commandInstance = (Command) constructor.newInstance(annotation.command());

        if (!annotation.description().isEmpty()) {
            commandInstance.setDescription(annotation.description());
        }
        if (!annotation.permission().isEmpty()) {
            commandInstance.setPermission(annotation.permission());
        }
        if (annotation.aliases().length > 0) {
            commandInstance.setAliases(Arrays.asList(annotation.aliases()));
        }
        return commandInstance;
    }

    /**
     * Helper Method to get the commandMap of the plugin instance
     * @return the paper commandMap of the server
     */
    private static CommandMap getCommandMap() {
        try {
            Server server = Bukkit.getServer();
            Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);
            return (CommandMap) getCommandMap.invoke(server);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get CommandMap", e);
        }
    }
}