package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Boolean> bypass = new HashMap<>();
    private final Map<UUID, Boolean> debug = new HashMap<>();
    private final Map<String, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    // === Claim Management ===

    public void claim(Player player) {
        String key = getChunkKey(player.getLocation());
        if (claims.containsKey(key)) {
            msg(player, "&cThis chunk is already claimed.");
            return;
        }
        claims.put(key, new Claim(player.getUniqueId(), key));
        msg(player, "&aYou claimed this chunk!");
    }

    public void unclaim(Player player) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null) {
            msg(player, "&cThis chunk is not claimed.");
            return;
        }
        if (!claim.getOwner().equals(player.getUniqueId()) && !isBypassing(player)) {
            msg(player, "&cYou don’t own this claim.");
            return;
        }
        claims.remove(key);
        msg(player, "&aChunk unclaimed.");
    }

    public void sendInfo(Player player) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null) {
            msg(player, "&eThis chunk is unclaimed.");
            return;
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayer(claim.getOwner());
        msg(player, "&7Owner: &b" + owner.getName());
        msg(player, "&7Trusted: &b" + claim.getTrusted().size());
    }

    // === Trust Management ===

    public void trust(Player player, String targetName, String role) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            msg(player, "&cYou don’t own this claim.");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        claim.getTrusted().put(target.getUniqueId(), role.toLowerCase(Locale.ROOT));
        msg(player, "&aTrusted &e" + targetName + " &7as role &b" + role);
    }

    public void untrust(Player player, String targetName) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            msg(player, "&cYou don’t own this claim.");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        claim.getTrusted().remove(target.getUniqueId());
        msg(player, "&cRemoved trust from &e" + targetName);
    }

    public void listTrusted(Player player) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null) {
            msg(player, "&cThis chunk is unclaimed.");
            return;
        }
        if (claim.getTrusted().isEmpty()) {
            msg(player, "&7No trusted players.");
            return;
        }
        msg(player, "&7Trusted players:");
        claim.getTrusted().forEach((uuid, role) -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            msg(player, " - &e" + p.getName() + " &7(" + role + ")");
        });
    }

    public void transferClaim(Player player, String targetName) {
        String key = getChunkKey(player.getLocation());
        Claim claim = claims.get(key);
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            msg(player, "&cYou don’t own this claim.");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        claim.setOwner(target.getUniqueId());
        msg(player, "&aTransferred claim to &e" + targetName);
    }

    // === Claim Preview ===

    public void previewClaim(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        int cx = loc.getChunk().getX() << 4;
        int cz = loc.getChunk().getZ() << 4;
        int y = loc.getBlockY();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100) cancel();
                for (int dx = 0; dx < 16; dx++) {
                    world.spawnParticle(Particle.VILLAGER_HAPPY, cx + dx, y, cz, 1);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, cx + dx, y, cz + 15, 1);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, cx, y, cz + dx, 1);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, cx + 15, y, cz + dx, 1);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        msg(player, "&7Showing claim preview...");
    }

    // === Admin Tools ===

    public void toggleBypass(Player player, String[] args) {
        boolean state = bypass.getOrDefault(player.getUniqueId(), false);
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "on" -> state = true;
                case "off" -> state = false;
                case "toggle" -> state = !state;
            }
        } else state = !state;
        bypass.put(player.getUniqueId(), state);
        msg(player, "&7Bypass mode: " + (state ? "&aON" : "&cOFF"));
    }

    public boolean isBypassing(Player player) {
        return bypass.getOrDefault(player.getUniqueId(), false);
    }

    public void purgeExpired(Player player, String[] args) {
        if (!player.hasPermission("proshield.admin.expired.purge")) {
            msg(player, "&cNo permission.");
            return;
        }
        int days = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        msg(player, "&7Purging claims older than &e" + days + " days...");
        // TODO: Expiry logic
    }

    public void toggleDebug(Player player, String[] args) {
        boolean state = debug.getOrDefault(player.getUniqueId(), false);
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "on" -> state = true;
                case "off" -> state = false;
                case "toggle" -> state = !state;
            }
        } else state = !state;
        debug.put(player.getUniqueId(), state);
        msg(player, "&7Debug mode: " + (state ? "&aON" : "&cOFF"));
    }

    // === Utilities ===

    private String getChunkKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    private void msg(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r ") + msg));
    }
}
