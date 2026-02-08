package de.einnik.boilerPlate.bind;

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