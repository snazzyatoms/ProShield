package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotManager.Claim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final GUIManager gui;
    private final PlotManager plotManager;

    public GUIListener(GUIManager gui, PlotManager plotManager) {
        this.gui = gui;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        String title = ChatColor.stripColor(inv.getTitle());
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        event.setCancelled(true); // prevent item movement in GUI

        // === MAIN MENU ===
        if (title.equalsIgnoreCase("ProShield Menu")) {
            switch (name.toLowerCase()) {
                case "claim chunk" -> plotManager.claimChunk(player);
                case "claim info" -> plotManager.showClaimInfo(player);
                case "unclaim chunk" -> plotManager.unclaimChunk(player);
                case "trust player" -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Type /proshield trust <player> in chat.");
                }
                case "untrust player" -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Type /proshield untrust <player> in chat.");
                }
                case "manage roles" -> {
                    Claim claim = plotManager.getClaimAt(player.getLocation());
                    if (claim != null && claim.isOwner(player.getUniqueId())) {
                        gui.openRoleMenu(player, claim);
                    } else {
                        player.sendMessage(ChatColor.RED + "You must be the claim owner to manage roles.");
                    }
                }
                case "toggle claim flags" -> {
                    Claim claim = plotManager.getClaimAt(player.getLocation());
                    if (claim != null && claim.isOwner(player.getUniqueId())) {
                        gui.openFlagMenu(player, claim);
                    } else {
                        player.sendMessage(ChatColor.RED + "You must be the claim owner to toggle flags.");
                    }
                }
                case "transfer ownership" -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Type /proshield transfer <player> in chat.");
                }
                case "help" -> player.performCommand("proshield help");
                case "admin tools" -> gui.openAdmin(player);
            }
        }

        // === ADMIN MENU ===
        else if (title.equalsIgnoreCase("ProShield Admin")) {
            switch (name.toLowerCase()) {
                case "fire toggle" -> plotManager.toggleFire(player);
                case "explosions toggle" -> plotManager.toggleExplosions(player);
                case "entity grief toggle" -> plotManager.toggleEntityGrief(player);
                case "interactions toggle" -> plotManager.toggleInteractions(player);
                case "pvp toggle" -> plotManager.togglePvP(player);
                case "keep items toggle" -> plotManager.toggleKeepItems(player);
                case "purge expired claims" -> player.performCommand("proshield purgeexpired 30 dryrun");
                case "reload config" -> {
                    Bukkit.getScheduler().runTask(gui.getPlugin(), () -> {
                        gui.getPlugin().reloadConfig();
                        player.sendMessage(ChatColor.GREEN + "ProShield config reloaded!");
                    });
                }
                case "debug mode" -> player.performCommand("proshield debug toggle");
                case "teleport tools" -> player.performCommand("proshield tp");
                case "back" -> gui.openMain(player, true);
                case "close" -> player.closeInventory();
            }
        }

        // === ROLE MENU ===
        else if (title.equalsIgnoreCase("Manage Roles")) {
            Claim claim = plotManager.getClaimAt(player.getLocation());
            if (claim == null) {
                player.sendMessage(ChatColor.RED + "You are not inside a claim.");
                return;
            }

            switch (name.toLowerCase()) {
                case "visitor" -> plotManager.setRole(player, "Visitor");
                case "member" -> plotManager.setRole(player, "Member");
                case "container" -> plotManager.setRole(player, "Container");
                case "builder" -> plotManager.setRole(player, "Builder");
                case "co-owner" -> plotManager.setRole(player, "Co-Owner");
                case "back" -> gui.openMain(player, false);
            }
        }

        // === FLAG MENU ===
        else if (title.equalsIgnoreCase("Claim Flags")) {
            Claim claim = plotManager.getClaimAt(player.getLocation());
            if (claim == null) {
                player.sendMessage(ChatColor.RED + "You are not inside a claim.");
                return;
            }

            switch (name.toLowerCase()) {
                case "pvp" -> plotManager.toggleFlag(player, claim, "pvp");
                case "explosions" -> plotManager.toggleFlag(player, claim, "explosions");
                case "fire" -> plotManager.toggleFlag(player, claim, "fire");
                case "enderman teleport" -> plotManager.toggleFlag(player, claim, "enderman-teleport");
                case "containers" -> plotManager.toggleFlag(player, claim, "containers");
                case "back" -> gui.openMain(player, false);
            }
        }
    }
}
