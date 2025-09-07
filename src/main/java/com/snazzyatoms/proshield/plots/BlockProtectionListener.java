package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    // cached toggles
    private boolean containers;
    private boolean mobGrief;
    private boolean creeper;
    private boolean tnt;
    private boolean wither;
    private boolean witherSkull;
    private boolean enderCrystal;
    private boolean enderDragon;

    private boolean fireSpread;
    private boolean fireBurn;
    private boolean igniteFlint;
    private boolean igniteLava;
    private boolean igniteLightning;
    private boolean igniteExplosion;
    private boolean igniteSpread;

    private boolean interactionsEnabled;
    private String interactionsMode; // blacklist | whitelist
    private final Set<Material> interactionSet = new HashSet<>();

    private boolean bucketEmptyBlock;
    private boolean bucketFillBlock;

    private boolean endermanGrief;
    private boolean ravagerGrief;
    private boolean silverfishGrief;
    private boolean dragonGrief;
    private boolean witherGrief;

    private boolean endermanTeleportDenied;

    public BlockProtectionListener(PlotManager plotManager) {
        this.plugin = ProShield.getInstance();
        this.plotManager = plotManager;
        reloadProtectionConfig();
    }

    /** Re-read config (called from /proshield reload) */
    public final void reloadProtectionConfig() {
        containers = plugin.getConfig().getBoolean("protection.containers", true);
        mobGrief  = plugin.getConfig().getBoolean("protection.mob-grief", true);

        creeper      = plugin.getConfig().getBoolean("protection.creeper-explosions", true);
        tnt          = plugin.getConfig().getBoolean("protection.tnt-explosions", true);
        wither       = plugin.getConfig().getBoolean("protection.wither-explosions", true);
        witherSkull  = plugin.getConfig().getBoolean("protection.wither-skull-explosions", true);
        enderCrystal
