package com.thencproject.papership.bind;

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

/**
 * Central class sending, if enabled in the plugins main
 * class into the chat to the operators when a logging action
 * is taken by this plugins logger
 */
@Getter
public class PaperShipLogger extends Logger {

    private final JavaPlugin plugin;
    private final Logger parentLogger;
    private final boolean debugEnabled;
    private final boolean verboseDebugEnabled;

    /**
     * Constructor with alle the parameters that defines the
     * Logging of the content
     * @param plugin the plugin the errors are broadcasted
     * @param debugEnabled normal debug is enabled
     * @param verboseDebugEnabled verbose debug is enabled
     */
    public PaperShipLogger(JavaPlugin plugin, boolean debugEnabled, boolean verboseDebugEnabled) {
        super(plugin.getName(), null);
        this.plugin = plugin;
        this.parentLogger = plugin.getLogger();
        this.debugEnabled = debugEnabled;
        this.verboseDebugEnabled = verboseDebugEnabled;

        setParent(parentLogger);
        setLevel(Level.ALL);

        addHandler(new BroadcastHandler());
    }

    /**
     * Java Logging subclass the log the message not only in
     * the console but also in-game but only to server operators
     */
    private class BroadcastHandler extends Handler {

        /**
         * Handler to handle which Level of issues we have and further
         * broadcasts it into the chat if enabled
         * @param record  description of the log event. A null record is
         *                 silently ignored and is not published
         */
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

    /**
     * Sends an in-game message for a normal info
     * @param message the message of the error
     */
    public void info(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.WHITE)
                    .append(Component.text("INFO: ", NamedTextColor.AQUA))
                    .append(Component.text(message, NamedTextColor.WHITE)));
        }
    }

    /**
     * Sends an in-game message for a warning
     * @param message the message of the error
     */
    public void warning(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.WHITE)
                    .append(Component.text("WARNING: ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                    .append(Component.text(message, NamedTextColor.RED)));
        }
    }

    /**
     * Sends an in-game message for a severe issue without
     * a stacktrace
     * @param message the message of the error
     */
    public void severe(String message) {
        if (debugEnabled || verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.WHITE)
                    .append(Component.text("ERROR: ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                    .append(Component.text(message, NamedTextColor.RED)));
        }
    }

    /**
     * Sends an in-game message for a severe issue
     * with 3 line long stacktrace
     * @param message the message of the error
     * @param throwable the throwable of the error
     */
    public void severe(String message, Throwable throwable) {

        if (debugEnabled || verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.WHITE)
                    .append(Component.text("ERROR: ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
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

    /**
     * Sends an in-game message for debug logging's
     * @param message the message of the log
     */
    public void debug(String message) {
        if (verboseDebugEnabled) {
            broadcastToOps(Component.text("[" + plugin.getName() + "] ", NamedTextColor.WHITE)
                    .append(Component.text("DEBUG: ", NamedTextColor.GREEN))
                    .append(Component.text(message, NamedTextColor.WHITE)));
        }
    }

    /**
     * Help method to only send messages to the server
     * operators
     * @param component the message that is sent
     */
    private void broadcastToOps(Component component) {
        Bukkit.getOnlinePlayers()
                .forEach(player -> player.sendMessage(component));
    }
}