package de.einnik.boilerPlate.bind;

import com.google.common.reflect.ClassPath;
import de.einnik.boilerPlate.annotations.*;
import de.einnik.boilerPlate.api.APIServiceRegistry;
import de.einnik.boilerPlate.api.PluginClassDoesNotImplementMethodsException;
import de.einnik.boilerPlate.debug.BoilerPlateLogger;
import de.einnik.boilerPlate.debug.ParentLoggerInitializeException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoilerPlateBootstrap {

    private static final Map<String, Object> PLUGIN_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, BoilerPlateLogger> LOGGER_REGISTRY = new ConcurrentHashMap<>();

    public static void initialize(JavaPlugin plugin) {
        Class<?> pluginClass = plugin.getClass();

        if (!pluginClass.isAnnotationPresent(BoilerPlatePlugin.class)) {
            throw new IllegalStateException("Plugin class must be annotated with @BoilerPlatePlugin");
        }

        boolean debugEnabled = pluginClass.isAnnotationPresent(EnableDebug.class);
        boolean verboseDebugEnabled = pluginClass.isAnnotationPresent(EnableVerboseDebug.class);
        BoilerPlateLogger bpLogger = new BoilerPlateLogger(plugin, debugEnabled, verboseDebugEnabled);

        injectLogger(plugin, bpLogger);

        PLUGIN_REGISTRY.put(plugin.getName(), plugin);
        LOGGER_REGISTRY.put(plugin.getName(), bpLogger);

        if (pluginClass.isAnnotationPresent(BoilerPlateAPI.class)) {
            registerAsAPI(plugin, pluginClass);
        }

        validateDependencies(plugin, pluginClass);

        if (pluginClass.isAnnotationPresent(EnableAutoRegistration.class)) {
            autoRegister(plugin, pluginClass);
        }
    }

    private static <T extends JavaPlugin> void registerAsAPI(T plugin, Class<?> pluginClass) {
        @SuppressWarnings("unchecked")
        Class<T> apiClass = (Class<T>) pluginClass;

        APIServiceRegistry.registerAPI(plugin, apiClass, plugin);
    }

    public static void shutdown(JavaPlugin plugin) {
        Class<?> pluginClass = plugin.getClass();

        try {
            if (pluginClass.isAnnotationPresent(BoilerPlateAPI.class)) {
                APIServiceRegistry.unregisterAllAPIs(plugin);
            }

            PLUGIN_REGISTRY.remove(plugin.getName());
            LOGGER_REGISTRY.remove(plugin.getName());

        } catch (Exception e) {
            throw new PluginClassDoesNotImplementMethodsException(e);
        }
    }

    private static void injectLogger(JavaPlugin plugin, BoilerPlateLogger customLogger) {
        try {
            Field loggerField = JavaPlugin.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(plugin, customLogger);

        } catch (Exception e) {
            throw new ParentLoggerInitializeException(e);
        }
    }

    private static void validateDependencies(JavaPlugin plugin, Class<?> pluginClass) {
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

            for (ClassPath.ClassInfo classInfo : classes) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());

                    if (clazz.isAnnotationPresent(AutoListener.class)) {
                        registerListener(plugin, clazz);
                    }

                    if (clazz.isAnnotationPresent(AutoCommand.class)) {
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
                            " must have a constructor with String parameter", e
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

    public static Object getPluginInstance(String name) {
        return PLUGIN_REGISTRY.get(name);
    }

    public static BoilerPlateLogger getLogger(String pluginName) {
        return LOGGER_REGISTRY.get(pluginName);
    }
}