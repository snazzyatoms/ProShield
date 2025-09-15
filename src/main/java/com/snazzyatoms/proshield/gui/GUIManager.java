package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.io.File; // ✅ FIX: Added missing import

/**
 * GUIManager
 * - Opens menus from config
 * - Handles dynamic state rendering (flags, world-controls)
 * - Wires Admin Tools (approve/deny requests, world controls)
 * - Restores chat-driven flows for:
 *     • Expansion deny (manual reason)
 *     • Roles add/remove via chat input
 * - Adds reset confirmation submenu with dynamic world title
 * - Permission-aware rendering (players vs admins vs senior admins)
 */
public class GUIManager {

    private final ProShield plugin;

    private static final List<String> PERM_KEYS = List.of("build", "interact", "containers", "unclaim");

    // ===== Chat-driven state =====
    private final Set<UUID> awaitingDenyReason = new HashSet<>();

    private static final class RoleActionCtx {
        enum Type { ADD, REMOVE }
        final Type type;
        final UUID plotId;
        RoleActionCtx(Type type, UUID plotId) {
            this.type = type;
            this.plotId = plotId;
        }
    }
    private final Map<UUID, RoleActionCtx> awaitingRoleAction = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // ... [rest of your existing GUIManager code unchanged] ...

    // Example where File is needed:
    private void saveConfigQuiet() {
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception ignored) {}
    }
}
