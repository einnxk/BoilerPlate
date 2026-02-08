package de.einnik.boilerPlate.bind;

import com.google.common.reflect.ClassPath;
import de.einnik.boilerPlate.annotations.*;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoilerPlateBootstrap {

    private static final Map<String, Object> PLUGIN_REGISTRY = new ConcurrentHashMap<>();

    public static void initialize(JavaPlugin plugin) {
        Class<?> pluginClass = plugin.getClass();

        if (!pluginClass.isAnnotationPresent(BoilerPlatePlugin.class)) {
            throw new InternalBootstrapException("Plugin class must be annotated with @BoilerPlatePlugin");
        }

        PLUGIN_REGISTRY.put(plugin.getName(), plugin);

        processDependencies(plugin, pluginClass);

        if (pluginClass.isAnnotationPresent(EnableAutoRegistration.class)) {
            autoRegister(plugin, pluginClass);
        }
    }

    private static void processDependencies(JavaPlugin plugin, Class<?> pluginClass) {
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
                continue;
            }

            if (!dependencyPlugin.isEnabled()) {
                String message = "Dependency '" + dep.name() + "' is not enabled!";
                if (dep.required()) {
                    throw new DependencyNotFoundException("Required " + message);
                }
                plugin.getLogger().warning(message);
                continue;
            }

            if (dep.joinClasspath()) {
                joinClasspath(plugin, dependencyPlugin);
            }
        }
    }

    private static void autoRegister(JavaPlugin plugin, Class<?> pluginClass) {
        EnableAutoRegistration config = pluginClass.getAnnotation(EnableAutoRegistration.class);
        String[] packages = config.scanPackages();

        if (packages.length == 0) {
            packages = new String[]{pluginClass.getPackageName()};
        }

        for (String packageName : packages) {
            scanAndRegister(plugin, packageName);
        }
    }

    private static void scanAndRegister(JavaPlugin plugin, String packageName) {
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            Set<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive(packageName);

            int listenerCount = 0;
            int commandCount = 0;

            for (ClassPath.ClassInfo classInfo : classes) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());

                    if (clazz.isAnnotationPresent(AutoListener.class)) {
                        registerListener(plugin, clazz);
                        listenerCount++;
                    }

                    if (clazz.isAnnotationPresent(AutoCommand.class)) {
                        registerCommand(plugin, clazz);
                        commandCount++;
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new InternalBootstrapException("Failed to scan for package", e);
        }
    }

    private static void registerListener(JavaPlugin plugin, Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) instance, plugin);
                plugin.getLogger().info("✓ Registered listener: " + clazz.getSimpleName());
            }
        } catch (Exception e) {
            throw new AutoRegistrationException("Error while instantiating listener " + clazz.getSimpleName(), e);
        }
    }

    private static void registerCommand(JavaPlugin plugin, Class<?> clazz) {
        try {
            AutoCommand annotation = clazz.getAnnotation(AutoCommand.class);

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

            CommandMap commandMap = getCommandMap();
            String fallbackPrefix = annotation.fallbackPrefix().isEmpty()
                    ? plugin.getName().toLowerCase()
                    : annotation.fallbackPrefix();

            commandMap.register(fallbackPrefix, commandInstance);
        } catch (NoSuchMethodException e) {
            throw new AutoRegistrationException(
                    "Command class " + clazz.getSimpleName() +
                            " must have a constructor with String parameter (command name)", e
            );
        } catch (Exception e) {
            throw new AutoRegistrationException(
                    "Error while instantiating command " + clazz.getSimpleName(), e
            );
        }
    }

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

    private static void joinClasspath(JavaPlugin plugin, Plugin dependency) {
        try {
            URLClassLoader pluginLoader = (URLClassLoader) plugin.getClass().getClassLoader();
            URLClassLoader depLoader = (URLClassLoader) dependency.getClass().getClassLoader();

            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);

            for (URL url : depLoader.getURLs()) {
                addURL.invoke(pluginLoader, url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getPluginInstance(String name) {
        return PLUGIN_REGISTRY.get(name);
    }
}