package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClaimRoleManager
 * - Manages trusted players and their roles inside claims
 * - Handles persistence (load/save)
 * - Provides helpers for chat-driven assignment
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    // Map<claimId, Map<playerUUID, role>>
    private final Map<String, Map<UUID, String>> claimRoles = new ConcurrentHashMap<>();

    private final File rolesFile;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
        this.rolesFile = new File(plugin.getDataFolder(), "roles.yml");
    }

    // ---------------------------
    // Persistence
    // ---------------------------

    public void loadAll() {
        if (!rolesFile.exists()) return;
        try {
            Properties props = new Properties();
            props.loadFromXML(new java.io.FileInputStream(rolesFile));

            for (String claimId : props.stringPropertyNames()) {
                String value = props.getProperty(claimId, "");
                Map<UUID, String> roleMap = new HashMap<>();
                if (!value.isEmpty()) {
                    String[] entries = value.split(";");
                    for (String entry : entries) {
                        String[] kv = entry.split(":");
                        if (kv.length == 2) {
                            try {
                                roleMap.put(UUID.fromString(kv[0]), kv[1]);
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                }
                claimRoles.put(claimId, roleMap);
            }
            plugin.getLogger().info("[ClaimRoleManager] Loaded roles from roles.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load roles.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        try {
            Properties props = new Properties();
            for (Map.Entry<String, Map<UUID, String>> entry : claimRoles.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<UUID, String> sub : entry.getValue().entrySet()) {
                    if (sb.length() > 0) sb.append(";");
                    sb.append(sub.getKey()).append(":").append(sub.getValue());
                }
                props.setProperty(entry.getKey(), sb.toString());
            }
            props.storeToXML(new java.io.FileOutputStream(rolesFile), "ProShield Roles");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save roles.yml: " + e.getMessage());
        }
    }

    // ---------------------------
    // Role Management
    // ---------------------------

    public boolean hasClaim(Player owner) {
        return plugin.getPlotManager().getClaimAt(owner.getLocation()) != null;
    }

    public void setRole(Player owner, UUID target, String role) {
        String claimId = plugin.getPlotManager().getClaimIdAt(owner.getLocation());
        if (claimId == null) return;
        claimRoles.computeIfAbsent(claimId, k -> new HashMap<>()).put(target, role);
        saveAll();
    }

    public void clearRole(Player owner, UUID target) {
        String claimId = plugin.getPlotManager().getClaimIdAt(owner.getLocation());
        if (claimId == null) return;
        Map<UUID, String> roles = claimRoles.get(claimId);
        if (roles != null) {
            roles.remove(target);
            if (roles.isEmpty()) {
                claimRoles.remove(claimId);
            }
        }
        saveAll();
    }

    public String getRole(Player owner, UUID target) {
        String claimId = plugin.getPlotManager().getClaimIdAt(owner.getLocation());
        if (claimId == null) return null;
        Map<UUID, String> roles = claimRoles.get(claimId);
        return roles != null ? roles.get(target) : null;
    }

    public boolean isValidRole(String role) {
        return plugin.getConfig().getConfigurationSection("roles") != null &&
                plugin.getConfig().getConfigurationSection("roles").getKeys(false).contains(role.toLowerCase());
    }

    // ---------------------------
    // Chat-driven role assignment
    // ---------------------------
    public void assignRoleViaChat(Player player, String message) {
        if (message == null || message.trim().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Invalid input.");
            return;
        }

        String[] parts = message.trim().split(" ");
        if (parts.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: <player> <role>");
            return;
        }

        String targetName = parts[0];
        String roleName = parts[1].toLowerCase();

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Could not find player: " + targetName);
            return;
        }

        if (!isValidRole(roleName)) {
            player.sendMessage(ChatColor.RED + "Invalid role: " + roleName);
            return;
        }

        if (!hasClaim(player)) {
            player.sendMessage(ChatColor.RED + "You are not inside a claim.");
            return;
        }

        setRole(player, target.getUniqueId(), roleName);

        // Messaging
        messages.send(player, "&aAssigned role &e" + roleName + " &ato " + target.getName());
        messages.send(target, "&eYou were assigned role &b" + roleName + " &eby " + player.getName());
    }

    // ---------------------------
    // Utility
    // ---------------------------
    public Map<UUID, String> getRolesForClaim(String claimId) {
        return claimRoles.getOrDefault(claimId, Collections.emptyMap());
    }
}
