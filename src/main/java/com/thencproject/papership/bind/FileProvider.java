package com.thencproject.papership.bind;

import com.google.common.reflect.ClassPath;
import com.thencproject.papership.annotations.AutoWiredFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Helper class to provide the Files at runtime marked
 * with @AutoWiredFile and provides the File if this
 * is set in the Annotation
 */
public class FileProvider {

    /**
     * Method that is called from the outside to process all classes
     * in the package here and further calls the helper methods
     * @param plugin the plugin that is called on
     * @param packageName the absolute package where there is searched
     *                    for fields annotated with the @AutoWiredFile
     */
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

    /**
     * Helper Method to get alle fields that are annotated in one
     * class and then calls the next method onto them
     * @param plugin the plugin the file is called by
     * @param clazz the class to check for the annotation
     * @return the count of fields processed successfully
     */
    private static int processClassFields(JavaPlugin plugin, Class<?> clazz) {
        int providedCount = 0;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoWiredFile.class)) {
                try {
                    provideFile(plugin, clazz, field);
                    providedCount++;
                } catch (Exception e) {
                    throw new FileCopyException(e);
                }
            }
        }

        return providedCount;
    }

    /**
     * Helper Method to further process to file provider
     * @param plugin the plugin the file is called by
     * @param clazz the class where the annotated fields are in
     * @param field the field that is annotated for providing
     * @throws Exception while coping files
     */
    private static void provideFile(JavaPlugin plugin, Class<?> clazz, Field field) throws Exception {
        AutoWiredFile annotation = field.getAnnotation(AutoWiredFile.class);

        if (field.getType() != File.class) {
            return;
        }

        field.setAccessible(true);

        Object instance = getOrCreateInstance(plugin, clazz, field);

        if (instance == null) return;

        File initialFile = (File) field.get(instance);

        if (initialFile == null) return;

        String fileName = initialFile.getPath();
        File targetFile = new File(plugin.getDataFolder(), fileName);

        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!targetFile.exists() && annotation.copyIfNotExists()) {
            copyFromResources(plugin, fileName, targetFile);
        }

        field.set(instance, targetFile);
    }

    /**
     * Tries to get an instance of the Class the file can only in injected
     * into a static field or else a field named instance should be in the
     * file
     * @param plugin the plugin the API is called on
     * @param clazz the class the annotated field is in
     * @param field the field that is annotated
     * @return an instance of that class
     */
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

    /**
     * Copy the File from the resource folder because it is not
     * in the plugins directory
     * @param plugin the instance of the plugin needed for the folder
     * @param resourcePath the file that is copied
     * @param targetFile the file that the content is pasted into
     */
    private static void copyFromResources(JavaPlugin plugin, String resourcePath, File targetFile) {
        try (InputStream resourceStream = plugin.getResource(resourcePath)) {
            if (resourceStream == null) return;

            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new FileCopyException(e);
        }
    }
}