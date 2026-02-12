package com.thencproject.papership.loader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class DependencyProvider {

    private static final Map<String, DependencyInfo> DEPENDENCIES = new HashMap<>();

    static {
        DEPENDENCIES.put("sql", new DependencyInfo(
                "mysql-connector-j",
                "com.mysql.cj.jdbc.Driver",
                "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar",
                "mysql-connector-j-8.3.0.jar"
        ));

        DEPENDENCIES.put("hikari", new DependencyInfo(
                "HikariCP",
                "com.zaxxer.hikari.HikariDataSource",
                "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar",
                "HikariCP-5.1.0.jar"
        ));
    }

    public static void provideDependencies(JavaPlugin plugin, boolean sql, boolean hikari) {
        if (sql) {
            provideDependency(plugin, "sql");
        }

        if (hikari) {
            provideDependency(plugin, "hikari");
        }
    }

    private static void provideDependency(JavaPlugin plugin, String key) {
        DependencyInfo info = DEPENDENCIES.get(key);
        if (info == null) {
            return;
        }

        if (isClassAvailable(info.testClass)) {
            return;
        }

        try {
            File libFile = downloadDependency(plugin, info);
            loadDependency(plugin, libFile);

            if (!(isClassAvailable(info.testClass))) {
                throw new DoubleDependencyException("Failed to provide Classpath");
            }

        } catch (Exception e) {
            throw new DeprecatedUrlException(e);
        }
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, DependencyProvider.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static File downloadDependency(JavaPlugin plugin, DependencyInfo info) throws Exception {
        File libsDir = new File(plugin.getDataFolder().getParentFile(), "BoilerPlate/libs");
        if (!libsDir.exists()) {
            libsDir.mkdirs();
        }

        File libFile = new File(libsDir, info.fileName);

        if (libFile.exists()) {
            return libFile;
        }

        URL url = new URL(info.downloadUrl);
        try (InputStream in = url.openStream()) {
            Path tempFile = Files.createTempFile("boilerplate-", ".jar");
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempFile, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return libFile;
    }

    private static void loadDependency(JavaPlugin plugin, File jarFile) throws Exception {
        plugin.getLogger().fine("Loading " + jarFile.getName() + " into classpath...");

        ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();

        boolean loaded = false;

        loaded = tryPaperPluginClassLoader(pluginClassLoader, jarFile, plugin);

        if (!loaded) {
            loaded = tryURLClassLoader(pluginClassLoader, jarFile, plugin);
        }

        if (!loaded) {
            loaded = trySystemClassLoader(jarFile, plugin);
        }

        if (!loaded) {
            throw new DoubleDependencyException("Failed to load " + jarFile.getName() + " into classpath");
        }
    }

    private static boolean tryPaperPluginClassLoader(ClassLoader loader, File jarFile, JavaPlugin plugin) {
        try {
            Class<?> paperLoaderClass = Class.forName("io.papermc.paper.plugin.loader.PluginClassLoader");

            if (paperLoaderClass.isInstance(loader)) {
                try {
                    var method = paperLoaderClass.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(loader, jarFile.toURI().toURL());
                    return true;
                } catch (NoSuchMethodException e) {
                    throw new DeprecatedUrlException(e);
                }
            }
        } catch (Exception e) {
            throw new DeprecatedUrlException("Paper PluginClassLoader not found", e);
        }
        return false;
    }

    private static boolean tryURLClassLoader(ClassLoader loader, File jarFile, JavaPlugin plugin) {
        try {
            if (loader instanceof URLClassLoader) {
                var method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(loader, jarFile.toURI().toURL());
                return true;
            }
        } catch (Exception e) {
            throw new DeprecatedUrlException(e);
        }
        return false;
    }

    private static boolean trySystemClassLoader(File jarFile, JavaPlugin plugin) {
        try {
            ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
            if (systemLoader instanceof URLClassLoader) {
                var method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(systemLoader, jarFile.toURI().toURL());
                return true;
            }
        } catch (Exception e) {
            throw new DeprecatedUrlException(e);
        }
        return false;
    }

    private record DependencyInfo(String name, String testClass, String downloadUrl, String fileName) { }
}