package com.thencproject.papership.bind;

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

/**
 * Helper class to provide the Dependencies at runtime marked
 * with @ImproviseDependency and provides the compiled classes
 * if this is set in the Annotation
 */
public class DependencyProvider {

    private static final Map<String, DependencyInfo> DEPENDENCIES = new HashMap<>();

    // static initializer to set the download URL's of the
    // dependencies
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

    /**
     * Method that is called from the outside that later calls further methods
     * to get the dependencies
     * @param plugin the plugin that calls
     * @param sql defines if the mysql-connector should be provided
     * @param hikari defines if hikariCP should be provided
     */
    public static void provideDependencies(JavaPlugin plugin, boolean sql, boolean hikari) {
        if (sql) {
            provideDependency(plugin, "sql");
        }

        if (hikari) {
            provideDependency(plugin, "hikari");
        }
    }

    /**
     * checks if the classpath is maybe already available and then
     * downloads if not
     * @param plugin the plugin that calls
     * @param key the identifier for the dependency
     */
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
                throw new IllegalStateException("Classpath is already available");
            }

        } catch (Exception e) {
            throw new DeprecatedUrlException(e);
        }
    }

    /**
     * Tries if the class is available at runtime
     * @param className absolute path for the class to check
     * @return if the class is available
     */
    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, DependencyProvider.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * downloads the dependencies
     * @param plugin the plugin this is called by
     * @param info a util class with the information about the dependency
     * @return the downloaded file
     * @throws Exception an exception while providing so
     */
    private static File downloadDependency(JavaPlugin plugin, DependencyInfo info) throws Exception {
        File libsDir = new File(plugin.getDataFolder().getParentFile(), "PaperShip/libs");
        if (!libsDir.exists()) {
            libsDir.mkdirs();
        }

        File libFile = new File(libsDir, info.fileName);

        if (libFile.exists()) {
            return libFile;
        }

        URL url = new URL(info.downloadUrl);
        try (InputStream in = url.openStream()) {
            Path tempFile = Files.createTempFile("papership-", ".jar");
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempFile, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return libFile;
    }

    /**
     * loads the dependencies into the classpath
     * @param plugin the plugin called by
     * @param jarFile the final jar file
     */
    private static void loadDependency(JavaPlugin plugin, File jarFile)  {
        ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();

        boolean loaded;

        loaded = tryPaperPluginClassLoader(pluginClassLoader, jarFile);

        if (!loaded) {
            loaded = tryURLClassLoader(pluginClassLoader, jarFile);
        }

        if (!loaded) {
            loaded = trySystemClassLoader(jarFile);
        }

        if (!loaded) {
            throw new RuntimeException();
        }
    }

    /**
     * @param loader tries to load the dependencies with the paper classloader
     * @param jarFile the final jar File
     * @return if the loading was successful
     */
    private static boolean tryPaperPluginClassLoader(ClassLoader loader, File jarFile) {
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

    /**
     * @param loader tries to use the inbuild java classloader
     * @param jarFile the final jar file
     * @return if the loading was successful
     */
    private static boolean tryURLClassLoader(ClassLoader loader, File jarFile) {
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

    /**
     * @param jarFile the final jar file
     * @return if the loading was successful
     */
    private static boolean trySystemClassLoader(File jarFile) {
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

    /**
     * Util class that represents a dependencies with its attributes
     * @param name the name of the dependency
     * @param testClass a class we can test if the dependency is available
     * @param downloadUrl the url for downloading the dependency
     * @param fileName the name of the file to download
     */
    private record DependencyInfo(String name, String testClass, String downloadUrl, String fileName) {}
}