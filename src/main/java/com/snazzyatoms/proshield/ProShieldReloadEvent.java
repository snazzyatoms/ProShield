package com.snazzyatoms.proshield;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired by ProShield after /proshield reload or Admin GUI reload.
 * Synchronous to ensure thread-safety with Bukkit API.
 */
public class ProShieldReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ProShield plugin;

    public ProShieldReloadEvent(ProShield plugin) {
        super(false); // sync event (safe for Bukkit API calls)
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
