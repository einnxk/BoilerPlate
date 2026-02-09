# BoilerPlate Version 1.21.8+
Vermeide es in deinen Plugins immer den selben Code zu schreiben.
<br>Nutze deine Zeit leiber mit spannenderen Tätigkeiten, als Listener oder Commands zu registrieren, jedoch auch das schreiben von Dependency Plugins oder API Plugins kann man vereinfachen.

> [!WARNING]
> **⚠️ Achtung ⚠️**
> <br>Diese API ist nur für moderne Paper-Plugins gedacht die eine `Paper-Plugin.yml` besitzt. Das bedeutet, das nur 
> Server die Paper oder einen Paper-Fork (z.B. Leaf) benutzen diese Plugins nutzen können. 
> <br>Andere Server-Software wird nicht zu unterstützt
## Vorteile & Features

- Keine Manuellen Commands & Listener registrieren
- Keine `paper-plugin.yml` mehr nötig, diese wird generiert
- Development Freundlicher Ingame Logger
- Kein extra Plugin wird auf dem Server benötigt
- Optionale Bereitstellung der `MySqlConnector` und der `HikariCP` Klassen, falsch gewünscht und nicht bereits auf dem Server
- Automatisch laden von Files aus dem Plugin ordner oder Optionales kopieren, falls diese dort nicht vorhanden sind aus dem Resources-Ordner des Plugins
- Automatisches Umwandeln eines Plugins in eine API ohne loading Issues mit der Plugin Instanz
---
## In Projekt einbinden 
Wir zeigen das ganze hier am Beispiel von Gradle, da Gradle mehr möglichkeiten bindet und moderner in verschiedenen Bereichen ist als Maven.

### Gradle
1.Repository in Gradle hinzufügen
```gradle
repositories {
    mavenCentral()
    maven {
        name = "BoilerPlate" // der name kann frei gesetzt werden
        url = uri("https://maven.pkg.github.com/Nikcraft-de-Development/GuiAPI")
    }
}
```
2.Dependencies und AnnotationProcessor hinzufügen
```gradle
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    // ${version} dabei durch den aktuellen Release ersetzen
    implementation 'com.github.einnik:boilerplate:${version}'
    annotationProcessor 'com.github.einnik:boilerplate:${version}'
}
```

## Damit Programmieren
