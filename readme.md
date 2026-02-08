tasks {
assemble {
dependsOn(reobfJar)
}

    processResources {
        // Wichtig: Lösche alte paper-plugin.yml falls vorhanden
        doFirst {
            val resourcesDir = file("src/main/resources")
            resourcesDir.resolve("paper-plugin.yml").delete()
        }
    }
}

java {
toolchain {
languageVersion.set(JavaLanguageVersion.of(17))
}
}