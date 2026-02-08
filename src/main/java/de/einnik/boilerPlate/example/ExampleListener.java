package de.einnik.boilerPlate.example;

import de.einnik.boilerPlate.annotations.AutoCommand;
import de.einnik.boilerPlate.annotations.AutoListener;
import org.bukkit.event.Listener;

@AutoCommand(
        command = "lobby",
        fallbackPrefix = "lobby",
        permission = "example.command.lobby",
        description = "example Command",
        aliases = {"l", "hub"}
)
@AutoListener
public class ExampleListener implements Listener {
}