package com.lunatech.killcash.utility;


import com.lunatech.killcash.AbstractKillCash;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AbstractKillCash#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AbstractKillCash#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AbstractKillCash.getInstance().getComponentLogger();
    }
}
