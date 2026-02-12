package com.thencproject.papership.loader;

import com.google.common.reflect.ClassPath;
import com.thencproject.papership.annotations.AutoProvideFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.logging.Level;

public class FileProvider {

    public static void provideFiles(JavaPlugin plugin, String packageName) {
        int providedCount = 0;
        int classesScanned = 0;

        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            Set<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive(packageName);

            for (ClassPath.ClassInfo classInfo : classes) {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
                    classesScanned++;

                    int filesInClass = processClassFields(plugin, clazz);
                    providedCount += filesInClass;

                } catch (ClassNotFoundException e) {
                    throw new FileCopyException(e);
                }
            }

        } catch (Exception e) {
            throw new FileCopyException(e);
        }
    }

    private static int processClassFields(JavaPlugin plugin, Class<?> clazz) {
        int providedCount = 0;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoProvideFile.class)) {
                try {
                    provideFile(plugin, clazz, field);
                    providedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Failed to provide file for field: " + clazz.getName() + "." + field.getName(), e);
                }
            }
        }

        return providedCount;
    }

    private static void provideFile(JavaPlugin plugin, Class<?> clazz, Field field) throws Exception {
        AutoProvideFile annotation = field.getAnnotation(AutoProvideFile.class);

        if (field.getType() != File.class) {
            plugin.getLogger().warning("@AutoProvideFile can only be used on File fields, skipping: " +
                    clazz.getName() + "." + field.getName());
            return;
        }

        field.setAccessible(true);

        Object instance = getOrCreateInstance(plugin, clazz, field);

        if (instance == null) {
            plugin.getLogger().fine("Skipping non-instantiable class: " + clazz.getName());
            return;
        }

        File initialFile = (File) field.get(instance);

        if (initialFile == null) {
            plugin.getLogger().warning("@AutoProvideFile field is null in " + clazz.getName() + "." + field.getName());
            return;
        }

        String fileName = initialFile.getPath();
        File targetFile = new File(plugin.getDataFolder(), fileName);

        plugin.getLogger().fine("Processing file: " + fileName + " (from " + clazz.getSimpleName() + ")");

        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                plugin.getLogger().fine("Created directory: " + parentDir.getPath());
            }
        }

        if (!targetFile.exists() && annotation.copyIfNotExists()) {
            copyFromResources(plugin, fileName, targetFile);
        }

        field.set(instance, targetFile);
    }

    private static Object getOrCreateInstance(JavaPlugin plugin, Class<?> clazz, Field field) {
        try {
            if (plugin.getClass().equals(clazz)) {
                return plugin;
            }

            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                return null;
            }

            try {
                var getInstanceMethod = clazz.getDeclaredMethod("getInstance");
                getInstanceMethod.setAccessible(true);
                Object instance = getInstanceMethod.invoke(null);
                if (instance != null) {
                    return instance;
                }
            } catch (NoSuchMethodException ex) {
                throw new FileCopyException(ex);
            }

            try {
                var instanceField = clazz.getDeclaredField("instance");
                if (java.lang.reflect.Modifier.isStatic(instanceField.getModifiers())) {
                    instanceField.setAccessible(true);
                    Object instance = instanceField.get(null);
                    if (instance != null) {
                        return instance;
                    }
                }
            } catch (NoSuchFieldException ex) {
                throw new FileCopyException(ex);
            }

            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new FileCopyException(e);
            }

        } catch (Exception e) {
            throw new FileCopyException(e);
        }
    }

    private static void copyFromResources(JavaPlugin plugin, String resourcePath, File targetFile) {
        try (InputStream resourceStream = plugin.getResource(resourcePath)) {
            if (resourceStream == null) {
                plugin.getLogger().fine("Resource not found in JAR: " + resourcePath + " - skipping copy");
                return;
            }

            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new FileCopyException(e);
        }
    }
}