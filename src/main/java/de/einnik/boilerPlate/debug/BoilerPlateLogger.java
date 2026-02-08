package de.einnik.boilerPlate.debug;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Getter
public class BoilerPlateLogger extends Logger{

    private final JavaPlugin plugin;
    private final Logger parentLogger;
    private final boolean debugEnabled;
    private final boolean verboseDebugEnabled;

    public BoilerPlateLogger(JavaPlugin plugin, boolean debugEnabled, boolean verboseDebugEnabled) {
        super(plugin.getName(), null);
        this.plugin = plugin;
        this.parentLogger = plugin.getLogger();
        this.debugEnabled = debugEnabled;
        this.verboseDebugEnabled = verboseDebugEnabled;

        setParent(parentLogger);
        setLevel(Level.ALL);

        addHandler(new BroadcastHandler());
    }

    private class BroadcastHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }

            Level level = record.getLevel();
            String message = record.getMessage();
            Throwable throwable = record.getThrown();

            if (level == Level.SEVERE) {
                if (debugEnabled || verboseDebugEnabled) {
                    severe(message, throwable);
                }
            }

            else if (level == Level.WARNING) {
                if (verboseDebugEnabled) {
                    warning(message);
                }
            }

            else if (level == Level.INFO) {
                if (verboseDebugEnabled) {
                    info(message);
                }
            }

            else if (verboseDebugEnabled) {
                debug(message);
            }
        }

        @Override
        public void flush() {
            // Not needed
        }

        @Override
        public void close() throws SecurityException {
            // Not needed
        }
    }

    public void info(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.GRAY)
                    .append(Component.text("INFO: ", NamedTextColor.WHITE))
                    .append(Component.text(message, NamedTextColor.GREEN)));
        }
    }

    public void warning(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.GRAY)
                    .append(Component.text("WARNING: ", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(message, NamedTextColor.YELLOW)));
        }
    }

    public void severe(String message) {
        if (debugEnabled || verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.GRAY)
                    .append(Component.text("ERROR: ", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(message, NamedTextColor.RED)));
        }
    }

    public void severe(String message, Throwable throwable) {

        if (debugEnabled || verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.GRAY)
                    .append(Component.text("ERROR: ", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(message, NamedTextColor.RED)));

            broadcastToOps(Component.text("  └─ ", NamedTextColor.RED)
                    .append(Component.text(throwable.getClass().getSimpleName() + ": ", NamedTextColor.RED))
                    .append(Component.text(throwable.getMessage() != null ? throwable.getMessage() : "No message", NamedTextColor.RED)));

            if (verboseDebugEnabled) {
                StackTraceElement[] stackTrace = throwable.getStackTrace();
                int limit = Math.min(3, stackTrace.length);
                for (int i = 0; i < limit; i++) {
                    StackTraceElement element = stackTrace[i];
                    broadcastToOps(Component.text("     at ", NamedTextColor.RED)
                            .append(Component.text(element.getClassName() + "." + element.getMethodName(), NamedTextColor.RED))
                            .append(Component.text("(" + element.getFileName() + ":" + element.getLineNumber() + ")", NamedTextColor.RED)));
                }
                if (stackTrace.length > 3) {
                    broadcastToOps(Component.text("     ... and " + (stackTrace.length - 3) + " more", NamedTextColor.RED));
                }
            }
        }
    }

    public void debug(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.GRAY)
                    .append(Component.text("DEBUG: ", NamedTextColor.AQUA))
                    .append(Component.text(message, NamedTextColor.WHITE)));
        }
    }

    private void broadcastToOps(Component component) {
        Bukkit.getOnlinePlayers()
                .forEach(player -> player.sendMessage(component));
    }
}