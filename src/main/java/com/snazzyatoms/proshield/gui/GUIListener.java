package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public GUIListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        event.setCancelled(true); // block item stealing

        switch (title) {
            case "🛡️ ProShield Menu" -> {
                switch (clicked.getType()) {
                    case GRASS_BLOCK -> player.performCommand("proshield claim");
                    case PAPER -> player.performCommand("proshield info");
                    case BARRIER -> player.performCommand("proshield unclaim");
                    case PLAYER_HEAD -> gui.openTrust(player);
                    case SKELETON_SKULL -> player.performCommand("proshield untrust"); // manual command
                    case BOOK -> gui.openRoles(player);
                    case REDSTONE_TORCH -> gui.openFlags(player);
                    case WRITABLE_BOOK -> gui.openTransfer(player);
                    case COMPASS -> player.performCommand("proshield help");
                    case NETHER_STAR -> {
                        if (player.hasPermission("proshield.admin")) {
                            gui.openAdmin(player);
                        }
                    }
                    default -> {}
                }
            }
            case "🤝 Trust Player" -> {
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    player.sendMessage("§aUse /proshield trust <player> to add someone.");
                } else if (clicked.getType() == Material.BARRIER) {
                    gui.openMain(player);
                }
            }
            case "📜 Manage Roles" -> {
                if (clicked.getType() == Material.BARRIER) {
                    gui.openMain(player);
                } else {
                    player.sendMessage("§eAssign roles with /proshield trust <player> <role>");
                }
            }
            case "⚑ Claim Flags" -> {
                if (clicked.getType() == Material.BARRIER) {
                    gui.openMain(player);
                } else {
                    player.sendMessage("§eToggle flags via config or /proshield reload.");
                }
            }
            case "🔑 Transfer Claim" -> {
                if (clicked.getType() == Material.WRITABLE_BOOK) {
                    player.sendMessage("§aUse /proshield transfer <player> to give ownership.");
                } else if (clicked.getType() == Material.BARRIER) {
                    gui.openMain(player);
                }
            }
            case "⚒️ ProShield Admin Menu" -> {
                switch (clicked.getType()) {
                    case FLINT_AND_STEEL -> player.performCommand("proshield admin fire");
                    case TNT -> player.performCommand("proshield admin explosions");
                    case ENDER_EYE -> player.performCommand("proshield admin entitygrief");
                    case REDSTONE -> player.performCommand("proshield admin interactions");
                    case DIAMOND_SWORD -> player.performCommand("proshield admin pvp");
                    case CHEST -> player.performCommand("proshield admin keepitems");
                    case BOOK -> player.performCommand("proshield purgeexpired 30 dryrun");
                    case BOOKSHELF -> player.performCommand("proshield help");
                    case COMMAND_BLOCK -> player.performCommand("proshield debug toggle");
                    case COMPASS -> player.performCommand("proshield admin compass");
                    case BEACON -> player.performCommand("proshield reload");
                    case ENDER_PEARL -> player.performCommand("proshield admin tp");
                    case BARRIER -> gui.openMain(player);
                    default -> {}
                }
            }
        }
    }
}
