package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * 1.2 scaffold: roles per claim (OWNER implicit via PlotManager),
 * managers and members stored in config under roles.<claimKey>.{managers|members}
 */
public class ClaimRolesManager {

    public enum Role { OWNER, MANAGER, MEMBER, NONE }

    private final ProShield plugin;
    private final PlotManager plotManager;

    // Cache: claimKey -> { managers, members }
    private final Map<String, Set<UUID>> managers = new HashMap<>();
    private final Map<String, Set<UUID>> members  = new HashMap<>();

    public ClaimRolesManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        reload();
    }

    public void reload() {
        managers.clear();
        members.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("roles");
        if (root == null) return;

        for (String claimKey : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(claimKey);
            if (sec == null) continue;

            Set<UUID> mng = new HashSet<>();
            for (String s : sec.getStringList("managers")) {
                try { mng.add(UUID.fromString(s)); } catch (Exception ignored) {}
            }
            managers.put(claimKey, mng);

            Set<UUID> mem = new HashSet<>();
            for (String s : sec.getStringList("members")) {
                try { mem.add(UUID.fromString(s)); } catch (Exception ignored) {}
            }
            members.put(claimKey, mem);
        }
    }

    public void saveAll() {
        plugin.getConfig().set("roles", null);
        for (String key : managers.keySet()) {
            List<String> m = managers.getOrDefault(key, Collections.emptySet()).stream().map(UUID::toString).toList();
            plugin.getConfig().set("roles." + key + ".managers", m);
        }
        for (String key : members.keySet()) {
            List<String> m = members.getOrDefault(key, Collections.emptySet()).stream().map(UUID::toString).toList();
            plugin.getConfig().set("roles." + key + ".members", m);
        }
        plugin.saveConfig();
    }

    public Role getRole(UUID player, String claimKey) {
        var claim = plotManager.getByKey(claimKey).orElse(null);
        if (claim == null) return Role.NONE;
        if (claim.getOwner().equals(player)) return Role.OWNER;
        if (managers.getOrDefault(claimKey, Collections.emptySet()).contains(player)) return Role.MANAGER;
        if (members.getOrDefault(claimKey, Collections.emptySet()).contains(player)) return Role.MEMBER;
        return Role.NONE;
    }

    public boolean setRole(String claimKey, UUID player, Role role) {
        // clear from both sets
        managers.computeIfAbsent(claimKey, k -> new HashSet<>()).remove(player);
        members.computeIfAbsent(claimKey, k -> new HashSet<>()).remove(player);

        switch (role) {
            case MANAGER -> managers.computeIfAbsent(claimKey, k -> new HashSet<>()).add(player);
            case MEMBER  -> members.computeIfAbsent(claimKey, k -> new HashSet<>()).add(player);
            default      -> { /* OWNER/NONE not stored here */ }
        }
        saveAll();
        return true;
    }

    public boolean canManage(UUID player, String claimKey) {
        Role r = getRole(player, claimKey);
        return r == Role.OWNER || r == Role.MANAGER;
    }

    public boolean canBuild(UUID player, String claimKey) {
        Role r = getRole(player, claimKey);
        return r == Role.OWNER || r == Role.MANAGER || r == Role.MEMBER;
    }
}
