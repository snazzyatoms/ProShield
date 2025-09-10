package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ClaimRoleManager {
    private final JavaPlugin plugin;

    private final Map<String, Integer> roleHierarchy = new HashMap<>();
    private final Map<String, Set<String>> rolePermissions = new HashMap<>();

    public ClaimRoleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    // ==========================
    // Load Roles & Permissions
    // ==========================

    public void loadFromConfig() {
        roleHierarchy.clear();
        rolePermissions.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("roles");

        if (section == null) {
            loadDefaults();
            return;
        }

        for (String role : section.getKeys(false)) {
            int rank = section.getInt(role + ".rank", 0);
            List<String> perms = section.getStringList(role + ".permissions");

            roleHierarchy.put(role.toLowerCase(), rank);
            rolePermissions.put(role.toLowerCase(), new HashSet<>(perms));
        }

        // Always ensure defaults exist
        ensureDefaults();
    }

    private void loadDefaults() {
        roleHierarchy.put("visitor", 0);
        roleHierarchy.put("member", 1);
        roleHierarchy.put("container", 2);
        roleHierarchy.put("builder", 3);
        roleHierarchy.put("co-owner", 4);

        rolePermissions.put("visitor", Set.of("walk"));
        rolePermissions.put("member", Set.of("walk", "interact"));
        rolePermissions.put("container", Set.of("walk", "interact", "open_containers"));
        rolePermissions.put("builder", Set.of("walk", "interact", "open_containers", "build"));
        rolePermissions.put("co-owner", Set.of("all"));
    }

    private void ensureDefaults() {
        if (!roleHierarchy.containsKey("visitor")) {
            roleHierarchy.put("visitor", 0);
            rolePermissions.put("visitor", Set.of("walk"));
        }
        if (!roleHierarchy.containsKey("member")) {
            roleHierarchy.put("member", 1);
            rolePermissions.put("member", Set.of("walk", "interact"));
        }
        if (!roleHierarchy.containsKey("container")) {
            roleHierarchy.put("container", 2);
            rolePermissions.put("container", Set.of("walk", "interact", "open_containers"));
        }
        if (!roleHierarchy.containsKey("builder")) {
            roleHierarchy.put("builder", 3);
            rolePermissions.put("builder", Set.of("walk", "interact", "open_containers", "build"));
        }
        if (!roleHierarchy.containsKey("co-owner")) {
            roleHierarchy.put("co-owner", 4);
            rolePermissions.put("co-owner", Set.of("all"));
        }
    }

    // ==========================
    // Role Checks
    // ==========================

    public int getRank(String role) {
        return roleHierarchy.getOrDefault(role.toLowerCase(), 0);
    }

    public boolean hasPermission(String role, String perm) {
        role = role.toLowerCase();

        if (rolePermissions.containsKey(role)) {
            Set<String> perms = rolePermissions.get(role);
            return perms.contains("all") || perms.contains(perm.toLowerCase());
        }

        return false;
    }

    public boolean canBuild(String role) {
        return hasPermission(role, "build");
    }

    public boolean canOpenContainers(String role) {
        return hasPermission(role, "open_containers");
    }

    public boolean canInteract(String role) {
        return hasPermission(role, "interact");
    }

    public boolean canDoEverything(String role) {
        return hasPermission(role, "all");
    }

    // ==========================
    // Utility
    // ==========================

    public Set<String> getDefinedRoles() {
        return Collections.unmodifiableSet(roleHierarchy.keySet());
    }

    public Map<String, Integer> getRoleHierarchy() {
        return Collections.unmodifiableMap(roleHierarchy);
    }

    public Map<String, Set<String>> getRolePermissions() {
        return Collections.unmodifiableMap(rolePermissions);
    }
}
