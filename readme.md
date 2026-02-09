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

## Damit Entwickeln
Wichtig, diese API ist kein eigenes ständiges Plugin und muss nicht auf dem Server laufen.
### Main Plugin Klasse als BoilerPlatePlugin markieren
Mit der Annotation `@BoilerPlatePlugin` wird dieses Plugin als ein Plugin mit reduziertem Code-Aufwand markiert, dabei ist zwingend notwendig dies auf die Main-Klasse eines Plugins anzuwenden. Alles andere wird einen Fehler werfen.
<br>Mit der Annotation `@EnableAutoRegistration` wird markiert, dass dieses Plugins alle Listener und Commands registriert die in einem hierarchical Package unter dem der Main Klasse sind. Als Beispiel das Package `de.einnik.exmaple.listeners` wäre ein valides Package für AutoListener. Das Package `de.einnik` wäre hingegen kein valides Package für AutoListener.
<br>Die Annotation `@PluginConfigurationFile` repräsentiert die Konfigurationsdatei eines Plugins, die `paper-plugin.yml` und lässt dich alles in der Main Klasse verwalten.
Dabei ist Wichtig zu wissen, deine paper-plugin.yml aus deinem Resource-Ordner wird überschrieben. Die Werte als Parameter der Annotation sind gleich auszufüllen wie in der Konfigurations-Datei des Plugins bis auf die `dependencies`. 
<br>Mit dependencies sind in diesem Fall Plugin dependencies gemeint, daher ist der `name` der Name des Plugins das auf dem Server sein muss. Der Rest ist gleich wie wenn man Plugins in die `paper-plugin.yml` einträgt.
<br>Wichtig bei der Initialisierung des Plugins ist das dieser Aufruf nicht zu früh kommt, das heißt nicht in der `onLoad` Funktion sondern in der `onEnable`.
```java
package de.einnik.example;

import de.einnik.boilerPlate.annotations.BoilerPlatePlugin;
import de.einnik.boilerPlate.annotations.EnableAutoRegistration;
import de.einnik.boilerPlate.annotations.PluginConfigurationFile;
import de.einnik.boilerPlate.annotations.PluginDependency;
import de.einnik.boilerPlate.bind.BoilerPlateProvider;
import de.einnik.boilerPlate.bind.PluginLoading;
import org.bukkit.plugin.java.JavaPlugin;

@BoilerPlatePlugin
@EnableAutoRegistration
@PluginConfigurationFile(
        name = "ExamplePlugin",
        version = "1.0",
        apiVersion = "1.21",
        loading = PluginLoading.STARTUP,
        author = "EinNik",
        description = "API to reduce BoilerPlate Code in your Plugins",
        website = "https://github.com/einnxk/BoilerPlate",
        dependencies = {
                @PluginDependency(
                       name = "LuckPerms",
                       load = false,
                       required = true,
                       joinClasspath = true 
                )
        }
)
public class ExamplePlugin extends JavaPlugin {
    
    @Override
    public void onEnable(){
        BoilerPlateProvider.initialize(this);
    }
}
```
Einen Listener zu definieren der Automatisch registriert wird ist nicht sehr schwer, wenn dieser in einem validen package liegt. 
<br>Dies Funktioniert mit dem Hinzufügen der Annotation `@AutoListener` an die Klasse die den Bukkit-Listener implimentiert.
```java
package de.einnik.boilerPlate.loader;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.einnik.boilerPlate.annotations.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AutoListener
public class ExampleListener implements Listener {
    
    @EventHandler
    public void onJump(PlayerJumpEvent e) {
        // do sth here
    }
}
```
Ähnlich ist das ganze für einen Command, wichtig für den Command ist es das es ein "moderner Command" ist, wie im Beispiel unten. 
<br>Hier muss die Klasse mit der Annotation `@AutoCommand` und den entsprechenden Parametern annotiert werden, damit dieser richtig registriert wird.
<br>Wichtig hierbei ist jedoch das der Constructor nie mehr als dieses `eine Argument hat`, alle anderen Attribute werde über die Annotation festgelegt.
```java
package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.AutoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AutoCommand(
        command = "example",
        fallbackPrefix = "example",
        permission = "example.command",
        description = "An example command",
        aliases = {"ex", "exemple"}
)
public class ExampleListener extends Command {

    public ExampleListener(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String @NotNull [] strings) {
        return false;
    }
}
```

### Debug Funktionen
Aus unserem Beispiel kennen wir ja schon das Grundkonstrukt eines BoilerPlate Plugins, wenn man nun keinen Zugriff auf die Konsole hat, auß welchen Gründen auch immer... - Dann hat man die Möglichkeit die Debug Logger zu nutzen.
<br>Die Annotation `@EnableDebug` sorgt dabei nur dafür das Fehler des Levels Severe in den Chat für alle Spieler gebroadcasted werden.
<br>Die Annotation `@EnableVerboseDebug` broadcasted hingegen jeden Fehler, oder jede Info in den Chat, die vom Plugin ausgehend ist. Dab ist keine der beiden Annotationen dazu gedacht während des Deployments da zu sein, sondern lediglich während des `Developements`.
```java
package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.*;
import de.einnik.boilerPlate.bind.BoilerPlateProvider;
import de.einnik.boilerPlate.bind.PluginLoading;
import org.bukkit.plugin.java.JavaPlugin;

@BoilerPlatePlugin
@EnableAutoRegistration
@EnableDebug // <- neu  
@EnableVerboseDebug // <- neu
@PluginConfigurationFile(
        name = "ExamplePlugin",
        version = "1.0",
        apiVersion = "1.21",
        loading = PluginLoading.STARTUP,
        author = "EinNik",
        description = "API to reduce BoilerPlate Code in your Plugins",
        website = "https://github.com/einnxk/BoilerPlate",
        dependencies = {
                @PluginDependency(
                       name = "LuckPerms",
                       load = false,
                       required = true,
                       joinClasspath = true
                )
        }
)
public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable(){
        BoilerPlateProvider.initialize(this);
    }
}
```

### Dependency-Loader
Hier werden während des `Developements` die Klassen des `MySqlConnector` und von `HikariCP` bereit gestellt. 
<br>Standardmäßig wird allerdings nicht bereits gestellt, um dies zu aktivieren müssen wir auf die Main Klasse des Plugins die Annotation `@EnableDependencyImprovisation`
anwenden, so werden standardmäßig beide dependencies provided mit den parametern, danach kann man einen der beiden deaktivieren.
<br>Keines Falles soll hiermit das `Shaden` dieser Dependencies ersetz werden, es soll lediglich dazu dienen Schwierigkeiten während des `Entwicklungsprozesses` zu vereinfachen.
```java
package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.*;
import de.einnik.boilerPlate.bind.BoilerPlateProvider;
import de.einnik.boilerPlate.bind.PluginLoading;
import org.bukkit.plugin.java.JavaPlugin;

@BoilerPlatePlugin
@EnableAutoRegistration
@EnableDependencyImprovisation(sql = false) // <- neu
@EnableDebug
@EnableVerboseDebug 
@PluginConfigurationFile(
        name = "ExamplePlugin",
        version = "1.0",
        apiVersion = "1.21",
        loading = PluginLoading.STARTUP,
        author = "EinNik",
        description = "API to reduce BoilerPlate Code in your Plugins",
        website = "https://github.com/einnxk/BoilerPlate",
        dependencies = {
                @PluginDependency(
                       name = "LuckPerms",
                       load = false,
                       required = true,
                       joinClasspath = true
                )
        }
)
public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable(){
        BoilerPlateProvider.initialize(this);
    }
}
```

### API's erstellen
Jetzt wird die Instanz des Plugins mit allen Feldern darin geladen und instanziert. Dabei wird die Klasse `ExamplePluginProvider` das Plugin heißt immer wie die Main Klasse mit dem Anhang `Provider`. In dieser Klasse gibt es dann die Methode `getAPI` die keine Parameter benötigt und mit der man die Instanz des Plugins bekommen kann. 
```java
package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.*;
import de.einnik.boilerPlate.bind.BoilerPlateProvider;
import de.einnik.boilerPlate.bind.PluginLoading;
import org.bukkit.plugin.java.JavaPlugin;

@BoilerPlatePlugin
@EnableAutoRegistration
@BoilerPlateAPI // <- neu
@EnableDependencyImprovisation(sql = false)
@EnableDebug
@EnableVerboseDebug
@PluginConfigurationFile(
        name = "ExamplePlugin",
        version = "1.0",
        apiVersion = "1.21",
        loading = PluginLoading.STARTUP,
        author = "EinNik",
        description = "API to reduce BoilerPlate Code in your Plugins",
        website = "https://github.com/einnxk/BoilerPlate",
        dependencies = {
                @PluginDependency(
                       name = "LuckPerms",
                       load = false,
                       required = true,
                       joinClasspath = true
                )
        }
)
public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable(){
        BoilerPlateProvider.initialize(this);
    }
    
    // neue Methode muss zwingend implimentiert werden
    @Override
    public void onDisable(){
        BoilerPlateProvider.shutdown(this);
    }
}
```
Unter Klassen der API's sind normale Klassen oder Enums die mit der Annotation `@SubAPI` annotiert wird. Man sieht daran das alle Methode als used markiert sind, wenn dies Erfolgreich ist.
```java
package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.*;

@SubAPI
public class ExamplePlugin {
    
    // API Methods here
}
```