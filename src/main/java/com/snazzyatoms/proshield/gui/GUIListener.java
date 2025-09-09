package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plots = plots;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        String title = ChatColor.stripColor(e.getView().getTitle());
        if (title == null) return;

        // We only care about our menus
        boolean isMain  = e.getView().getTitle().equals(GUIManager.TITLE_MAIN);
        boolean isAdmin = e.getView().getTitle().equals(GUIManager.TITLE_ADMIN);
        boolean isHelp  = e.getView().getTitle().equals(GUIManager.TITLE_HELP);
        if (!isMain && !isAdmin && !isHelp) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;
        Material type = e.getCurrentItem().getType();

        if (isMain) {
            switch (type) {
                case GRASS_BLOCK: // claim
                    p.performCommand("proshield claim");
                    p.closeInventory();
                    break;
                case OAK_SIGN: // info
                    p.performCommand("proshield info");
                    p.closeInventory();
                    break;
                case BARRIER: // unclaim
                    p.performCommand("proshield unclaim");
                    p.closeInventory();
                    break;
                case NETHER_STAR: // admin
                    if (p.hasPermission("proshield.admin.gui")) {
                        gui.openAdmin(p);
                    } else {
                        p.sendMessage(ProShield.getInstance().getConfig().getString("messages.prefix", "")
                                + ChatColor.RED + "You don’t have permission to open Admin menu.");
                    }
                    break;
                case BOOK: // help
                    gui.openHelp(p);
                    break;
                case ARROW: // back
                    p.closeInventory();
                    break;
                default:
                    break;
            }
            return;
        }

        if (isHelp) {
            if (type == Material.ARROW) {
                gui.openMain(p);
            }
            return;
        }

        if (isAdmin) {
            // toggle actions are performed via commands to centralize logic
            switch (type) {
                case FLINT_AND_STEEL: // fire toggle
                    p.performCommand("proshield admin toggle fire");
                    gui.openAdmin(p);
                    break;
                case TNT: // explosions
                    p.performCommand("proshield admin toggle explosions");
                    gui.openAdmin(p);
                    break;
                case LEVER: // interactions
                    p.performCommand("proshield admin toggle interactions");
                    gui.openAdmin(p);
                    break;
                case CREEPER_HEAD: // mob grief
                    p.performCommand("proshield admin toggle mobgrief");
                    gui.openAdmin(p);
                    break;
                case IRON_SWORD:
                case SHIELD: // PvP
                    p.performCommand("proshield admin toggle pvp");
                    gui.openAdmin(p);
                    break;
                case LAVA_BUCKET: // purge expired (just opens help/toast)
                    p.closeInventory();
                    p.sendMessage(ProShield.getInstance().getConfig().getString("messages.prefix", "")
                            + ChatColor.YELLOW + "Use /proshield purgeexpired <days> [dryrun]");
                    break;
                case LIME_DYE: // keep items in claims
                    p.performCommand("proshield admin toggle keepitems");
                    gui.openAdmin(p);
                    break;
                case REDSTONE_TORCH: // debug
                    p.performCommand("proshield debug toggle");
                    gui.openAdmin(p);
                    break;
                case REPEATER: // reload
                    p.performCommand("proshield reload");
                    gui.openAdmin(p);
                    break;
                case WRITABLE_BOOK: // admin help (same as help)
                    gui.openHelp(p);
                    break;
                case ARROW: // back
                    gui.openMain(p);
                    break;
                default:
                    break;
            }
        }
    }
}
