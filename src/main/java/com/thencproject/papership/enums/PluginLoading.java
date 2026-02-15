package com.thencproject.papership.enums;

/**
 * Enum class representing when a paper-plugin.yml
 * attribute when the plugin should be loaded
 * The Options are on the start of the Server or after
 * the world is loaded, if this plugin depends on many
 * other Plugins POSTWORLD is the better option
 */
public enum PluginLoading {

    STARTUP("STARTUP"),
    POSTWORLD("POSTWORLD");

    private final String id;

    PluginLoading(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}