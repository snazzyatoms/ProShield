package com.snazzyatoms.proshield;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired by ProShield after /proshield reload or Admin GUI reload. */
public class ProShieldReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ProShield plugin;

    public ProShieldReloadEvent(ProShield plugin) {
        super(true); // async=false would also be fine; true marks it as synchronous=false
        this.plugin = plugin;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
