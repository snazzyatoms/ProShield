package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    private boolean isTitle(InventoryClickEvent e, String title) {
        if (e.getView() == null) return false;
        String t;
        try {
            t = e.getView().getTitle();
        } catch (Throwable ex) {
            return false;
        }
        return t != null && ChatColor.stripColor(t).equals(ChatColor.stripColor(title));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player)e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // MAIN
        if (isTitle(e, GUIManager.TITLE_MAIN)) {
            e.setCancelled(true);
            handleMainClick(p, clicked);
            return;
        }

        // ADMIN
        if (isTitle(e, GUIManager.TITLE_ADMIN)) {
            e.setCancelled(true);
            handleAdminClick(p, clicked);
            return;
        }

        // HELP
        if (isTitle(e, GUIManager.TITLE_HELP)) {
            e.setCancelled(true);
            if (isArrow(clicked)) {
                gui.openMain(p);
            }
            return;
        }

        // TRUST
        if (isTitle(e, GUIManager.TITLE_TRUST)) {
            e.setCancelled(true);
            String name = name(clicked);
            if (name.contains("Back")) {
                gui.openMain(p);
                return;
            }
            if (name.contains("Trust Nearby")) {
                p.closeInventory();
                p.performCommand("proshield trustnearby");
                return;
            }
            if (name.contains("Trust by Name")) {
                p.closeInventory();
                p.sendMessage(color("&7Type &e/playername &7in chat…"));
                p.performCommand("proshield trustprompt");
                return;
            }
            if (name.contains("Set Role")) {
                p.closeInventory();
                p.performCommand("proshield roles");
                return;
            }
            return;
        }

        // TRANSFER
        if (isTitle(e, GUIManager.TITLE_TRANSFER)) {
            e.setCancelled(true);
            String name = name(clicked);
            if (name.contains("Back")) {
                gui.openMain(p);
                return;
            }
            if (name.contains("Transfer by Name")) {
                p.closeInventory();
                p.sendMessage(color("&7Type &e/playername &7in chat…"));
                p.performCommand("proshield transferprompt");
                return;
            }
            if (name.contains("Transfer to Nearby")) {
                p.closeInventory();
                p.performCommand("proshield transfernearby");
                return;
            }
        }
    }

    private void handleMainClick(Player p, ItemStack clicked) {
        String d = name(clicked);

        if (d.contains("Claim Chunk")) {
            p.closeInventory();
            p.performCommand("proshield claim");
            return;
        }
        if (d.contains("Claim Info")) {
            p.closeInventory();
            p.performCommand("proshield info");
            return;
        }
        if (d.contains("Unclaim")) {
            p.closeInventory();
            p.performCommand("proshield unclaim");
            return;
        }
        if (d.contains("Trust Player")) {
            gui.openTrustMenu(p);
            return;
        }
        if (d.contains("Roles")) {
            p.closeInventory();
            p.performCommand("proshield roles");
            return;
        }
        if (d.contains("Transfer Claim")) {
            gui.openTransferMenu(p);
            return;
        }
        if (d.contains("Claim Border Preview")) {
            p.closeInventory();
            p.performCommand("proshield preview 10");
            return;
        }
        if (d.contains("Settings")) {
            p.closeInventory();
            p.performCommand("proshield settings");
            return;
        }
        if (d.contains("Admin Tools")) {
            if (isAdmin(p)) {
                gui.openAdmin(p);
            } else {
                p.sendMessage(color("&cYou don’t have permission to open Admin Tools."));
            }
            return;
        }
        if (d.contains("Help")) {
            gui.openHelp(p, isAdmin(p));
            return;
        }
        if (d.equalsIgnoreCase("Back")) {
            p.closeInventory();
        }
    }

    private void handleAdminClick(Player p, ItemStack clicked) {
        if (!isAdmin(p)) {
            p.sendMessage(color("&cYou don’t have permission to edit Admin settings."));
            return;
        }

        String d = name(clicked);
        var cfg = ProShield.getInstance().getConfig();
        boolean changed = false;

        // Toggles (left click)
        if (d.contains("Explosions Protection")) {
            boolean cur = cfg.getBoolean("protection.explosions.enabled", true);
            cfg.set("protection.explosions.enabled", !cur);
            changed = true;
        } else if (d.contains("Fire Protection")) {
            boolean cur = cfg.getBoolean("protection.fire.enabled", true);
            cfg.set("protection.fire.enabled", !cur);
            changed = true;
        } else if (d.contains("Entity Griefing")) {
            boolean cur = cfg.getBoolean("protection.entity-grief.enabled", true);
            cfg.set("protection.entity-grief.enabled", !cur);
            changed = true;
        } else if (d.contains("Block PvP in Claims")) {
            boolean pvpBlocked = !cfg.getBoolean("protection.pvp-in-claims", false); // tile shows "Block PvP"
            cfg.set("protection.pvp-in-claims", !pvpBlocked ? false : true);
            // Re-evaluate: we store pvp-in-claims meaning "true = allow pvp". We want tile to flip "block pvp".
            // Simpler: if currently allow pvp (true), set false to block; else set true to allow.
            boolean allowNow = cfg.getBoolean("protection.pvp-in-claims", false);
            cfg.set("protection.pvp-in-claims", !allowNow);
            changed = true;
        } else if (d.contains("Keep Dropped Items")) {
            boolean cur = cfg.getBoolean("claims.keep-items.enabled", false);
            cfg.set("claims.keep-items.enabled", !cur);
            changed = true;
        } else if (d.contains("Drop Compass If Full")) {
            boolean cur = cfg.getBoolean("compass.drop-if-full", true);
            cfg.set("compass.drop-if-full", !cur);
            changed = true;
        } else if (d.contains("Admin Help")) {
            p.sendMessage(color("&7Admin tips: Left-click tiles to toggle. Values save instantly."));
            p.sendMessage(color("&7More bulk tools, town flags & audits are planned for &b2.0&7."));
        } else if (d.contains("Back")) {
            gui.openMain(p);
            return;
        }

        if (changed) {
            ProShield.getInstance().saveConfig();
            // refresh screen to reflect ON/OFF glass
            Bukkit.getScheduler().runTask(ProShield.getInstance(), () -> gui.openAdmin(p));
        }
    }

    private boolean isAdmin(Player p) {
        return p.isOp() || p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui");
    }

    private boolean isArrow(ItemStack it) {
        return it.getType() == Material.ARROW;
    }

    private String name(ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return "";
        if (!it.hasItemMeta() || it.getItemMeta().getDisplayName() == null) return it.getType().name();
        return ChatColor.stripColor(it.getItemMeta().getDisplayName());
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
