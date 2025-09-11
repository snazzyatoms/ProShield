package com.snazzyatoms.proshield;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired by ProShield after /proshield reload or Admin GUI reload.
 */
public class ProShieldReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ProShield plugin;

    public ProShieldReloadEvent(ProShield plugin) {
        super(false); // false = synchronous event
        this.plugin = plugin;
    }

    public ProShield getPlugin() {
        return plugin;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
