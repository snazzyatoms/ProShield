package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {

    private final ProShield plugin;
    private final Map<Chunk, Claim> claims = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* -----------------------------------------------------
     * Claim Operations
     * --------------------------------------------------- */
    public void claim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (claims.containsKey(chunk)) {
            player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
            return;
        }
        Claim claim = new Claim(player.getUniqueId(), chunk);
        claims.put(chunk, claim);
        player.sendMessage(ChatColor.GREEN + "You claimed this chunk.");
    }

    public void unclaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "This chunk is not claimed.");
            return;
        }
        if (!claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You do not own this claim.");
            return;
        }
        claims.remove(chunk);
        player.sendMessage(ChatColor.YELLOW + "You unclaimed this chunk.");
    }

    public void showInfo(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "This chunk is unclaimed.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Owner: " + Bukkit.getOfflinePlayer(claim.getOwner()).getName());
        player.sendMessage(ChatColor.AQUA + "Trusted: " + claim.getTrusted().size());
    }

    /* -----------------------------------------------------
     * Trust System
     * --------------------------------------------------- */
    public void trust(Player player, String targetName) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null || !claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You must own this claim.");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        claim.getTrusted().put(target.getUniqueId(), "Member");
        player.sendMessage(ChatColor.GREEN + "Trusted " + target.getName() + " as Member.");
    }

    public void untrust(Player player, String targetName) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null || !claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You must own this claim.");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        claim.getTrusted().remove(target.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Untrusted " + target.getName() + ".");
    }

    public void listTrusted(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "This chunk is unclaimed.");
            return;
        }
        if (claim.getTrusted().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No trusted players.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Trusted players:");
        claim.getTrusted().forEach((uuid, role) ->
                player.sendMessage("- " + Bukkit.getOfflinePlayer(uuid).getName() + " (" + role + ")"));
    }

    /* -----------------------------------------------------
     * Ownership Transfer
     * --------------------------------------------------- */
    public void transfer(Player player, String targetName) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null || !claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You must own this claim.");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        claim.setOwner(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Transferred claim to " + target.getName() + ".");
    }

    /* -----------------------------------------------------
     * Role Management
     * --------------------------------------------------- */
    public void assignRole(Player player, String role) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null || !claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You must own this claim.");
            return;
        }

        // Assign role to last trusted player (for simplicity in GUI prototype)
        UUID lastTrusted = claim.getTrusted().keySet().stream().reduce((first, second) -> second).orElse(null);
        if (lastTrusted == null) {
            player.sendMessage(ChatColor.RED + "No trusted players to assign a role.");
            return;
        }

        claim.getTrusted().put(lastTrusted, role);
        player.sendMessage(ChatColor.GREEN + "Set role of " + Bukkit.getOfflinePlayer(lastTrusted).getName() + " to " + role + ".");
    }

    /* -----------------------------------------------------
     * Claim Flags
     * --------------------------------------------------- */
    public void toggleFlag(Player player, String flag) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = claims.get(chunk);
        if (claim == null || !claim.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You must own this claim.");
            return;
        }

        boolean current = claim.getFlags().getOrDefault(flag, false);
        claim.getFlags().put(flag, !current);
        player.sendMessage(ChatColor.YELLOW + "Flag " + flag + " set to " + !current + ".");
    }

    /* -----------------------------------------------------
     * Previews / Expiry
     * --------------------------------------------------- */
    public void preview(Player player) {
        player.sendMessage(ChatColor.GRAY + "Showing claim preview (particles in 2.0).");
    }

    public void purgeExpired(Player player) {
        // Stub logic
        player.sendMessage(ChatColor.RED + "Expired claims purged (simulation).");
    }

    /* -----------------------------------------------------
     * Accessors
     * --------------------------------------------------- */
    public ProShield getPlugin() {
        return plugin;
    }

    public Map<Chunk, Claim> getClaims() {
        return claims;
    }

    /* -----------------------------------------------------
     * Claim Data Class
     * --------------------------------------------------- */
    public static class Claim {
        private UUID owner;
        private final Chunk chunk;
        private final Map<UUID, String> trusted = new HashMap<>();
        private final Map<String, Boolean> flags = new HashMap<>();

        public Claim(UUID owner, Chunk chunk) {
            this.owner = owner;
            this.chunk = chunk;
        }

        public boolean isOwner(Player player) {
            return player.getUniqueId().equals(owner);
        }

        public UUID getOwner() {
            return owner;
        }

        public void setOwner(UUID owner) {
            this.owner = owner;
        }

        public Map<UUID, String> getTrusted() {
            return trusted;
        }

        public Map<String, Boolean> getFlags() {
            return flags;
        }

        public Chunk getChunk() {
            return chunk;
        }
    }
}
