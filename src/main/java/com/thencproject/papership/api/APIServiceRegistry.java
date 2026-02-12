package com.thencproject.papership.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class APIServiceRegistry {

    private static final Map<Class<?>, Object> API_INSTANCES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Plugin> API_PROVIDERS = new ConcurrentHashMap<>();

    public static <T> void registerAPI(Plugin plugin, Class<T> apiClass, T apiInstance) {
        ServicesManager servicesManager = Bukkit.getServicesManager();


        servicesManager.register(apiClass, apiInstance, plugin, ServicePriority.Normal);

        API_INSTANCES.put(apiClass, apiInstance);
        API_PROVIDERS.put(apiClass, plugin);
    }

    public static void unregisterAllAPIs(Plugin plugin) {
        ServicesManager servicesManager = Bukkit.getServicesManager();

        API_PROVIDERS.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(plugin)) {
                Class<?> apiClass = entry.getKey();
                Object instance = API_INSTANCES.get(apiClass);

                if (instance != null) {
                    servicesManager.unregister(apiClass, instance);
                    API_INSTANCES.remove(apiClass);
                    plugin.getLogger().info("✓ Unregistered API: " + apiClass.getSimpleName());
                }

                return true;
            }
            return false;
        });
    }
}