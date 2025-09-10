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
            player.sendMessage
