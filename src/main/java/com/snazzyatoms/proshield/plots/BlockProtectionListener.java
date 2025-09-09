package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Core anti-grief protection inside claims:
 * - Break/place/build requires role >= BUILDER (or owner) unless bypass
 * - Interactions governed by blacklist/whitelist mode
 * - Buckets blocked if configured
 * - Fire spread/ignite blocked if configured
 * - Explosions & entity grief blocked if configured
 * - Enderman teleport deny inside claims if configured
 *
 * All defaults are SAFE (block most grief) as per 1.2.4 config.
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;

    // toggles
    private boolean containersProtected;
    private boolean interactionsEnabled;
    private String  interactionsMode;
    private Set<String> interactionCategories;
    private boolean bucketsEmptyBlocked;
    private boolean bucketsFillBlocked;

    private boolean fireSpread;
    private boolean fireBurn;
    private boolean fireIgniteFlint;
    private boolean fireIgniteLava;
    private boolean fireIgniteLightning;
    private boolean fireIgniteExplosion;
    private boolean fireIgniteSpread;

    private boolean explosionCreeper;
    private boolean explosionTNT;
    private boolean explosionWither;
    private boolean explosionWitherSkull;
    private boolean explosionEndCrystal;
    private boolean explosionDragon;

    private boolean entityGriefEnderman;
    private boolean entityGriefRavager;
    private boolean entityGriefSilverfish;
    private boolean entityGriefDragon;
    private boolean entityGriefWither;

    private boolean endermanTeleportDeny;

    public BlockProtectionListener(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots  = plots;
        reloadProtectionConfig();
    }

    public void reloadProtectionConfig() {
        FileConfiguration c = plugin.getConfig();

        containersProtected   = c.getBoolean("protection.containers", true);

        interactionsEnabled   = c.getBoolean("protection.interactions.enabled", true);
        interactionsMode      = c.getString ("protection.interactions.mode", "blacklist").toLowerCase();
        interactionCategories = new java.util.HashSet<>(c.getStringList("protection.interactions.categories"));

        bucketsEmptyBlocked   = c.getBoolean("protection.buckets.block-empty", true);
        bucketsFillBlocked    = c.getBoolean("protection.buckets.block-fill", true);

        // fire
        fireSpread            = c.getBoolean("protection.fire.spread", true);
        fireBurn              = c.getBoolean("protection.fire.burn", true);
        fireIgniteFlint       = c.getBoolean("protection.fire.ignite.flint_and_steel", true);
        fireIgniteLava        = c.getBoolean("protection.fire.ignite.lava", true);
        fireIgniteLightning   = c.getBoolean("protection.fire.ignite.lightning", true);
        fireIgniteExplosion   = c.getBoolean("protection.fire.ignite.explosion", true);
        fireIgniteSpread      = c.getBoolean("protection.fire.ignite.spread", true);

        // explosions
        explosionCreeper      = c.getBoolean("protection.creeper-explosions", true);
        explosionTNT          = c.getBoolean("protection.tnt-explosions", true);
        explosionWither       = c.getBoolean("protection.wither-explosions", true);
        explosionWitherSkull  = c.getBoolean("protection.wither-skull-explosions", true);
        explosionEndCrystal   = c.getBoolean("protection.ender-crystal-explosions", true);
        explosionDragon       = c.getBoolean("protection.ender-dragon-explosions", true);

        // entity grief
        entityGriefEnderman   = c.getBoolean("protection.entity-grief.enderman", true);
        entityGriefRavager    = c.getBoolean("protection.entity-grief.ravager", true);
        entityGriefSilverfish = c.getBoolean("protection.entity-grief.silverfish", true);
        entityGriefDragon     = c.getBoolean("protection.entity-grief.ender-dragon", true);
        entityGriefWither     = c.getBoolean("protection.entity-grief.wither", true);

        // teleport
        endermanTeleportDeny  = c.getBoolean("protection.entity-teleport.enderman", true);
    }

    /* =========================================================
       Build / Break — require role >= BUILDER (or owner) in claim
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("proshield.bypass")) return;
        if (!isInClaim(e.getBlock())) return;

        if (!hasBuildPermission(p, e.getBlock())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("proshield.bypass")) return;
        if (!isInClaim(e.getBlock())) return;

        if (!hasBuildPermission(p, e.getBlock())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    private boolean hasBuildPermission(Player p, Block b) {
        if (plots.isOwner(p.getUniqueId(), b.getLocation())) return true;
        try {
            return plots.hasRoleAtLeast(b.getLocation(), p.getUniqueId(), ClaimRole.BUILDER);
        } catch (Throwable ignored) {
            // If roles are not active, fall back to "trusted"
            return plots.isTrustedOrOwner(p.getUniqueId(), b.getLocation());
        }
    }

    /* =========================================================
       Interactions: doors/buttons/etc., containers
       ========================================================= */

    private static final Set<Material> CONTAINER_TYPES = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.FURNACE,
            Material.BLAST_FURNACE, Material.SMOKER, Material.DROPPER, Material.DISPENSER,
            Material.HOPPER, Material.SHULKER_BOX
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Player p = e.getPlayer();
        if (p.hasPermission("proshield.bypass")) return;
        Block b = e.getClickedBlock();
        if (!isInClaim(b)) return;

        // Containers always protected when enabled
        if (containersProtected && CONTAINER_TYPES.contains(b.getType())) {
            if (!canBasicInteract(p, b)) {
                e.setCancelled(true);
                deny(p);
                return;
            }
        }

        if (!interactionsEnabled) return;

        // If blacklist mode => block listed categories for non-trusted
        // If whitelist mode => allow only listed categories for non-trusted
        boolean isListedCategory = isCategory(b.getType());
        boolean allowedForNonTrusted =
                interactionsMode.equals("blacklist") ? !isListedCategory : isListedCategory;

        if (!isTrusted(p, b) && !allowedForNonTrusted) {
            e.setCancelled(true);
            deny(p);
        }
    }

    private boolean isCategory(Material type) {
        String name = type.name().toLowerCase();
        // quick category checks
        if (name.contains("door") && interactionCategories.contains("doors")) return true;
        if (name.contains("trapdoor") && interactionCategories.contains("trapdoors")) return true;
        if (name.contains("fence_gate") && interactionCategories.contains("fence_gates")) return true;
        if ((name.contains("button")) && interactionCategories.contains("buttons")) return true;
        if ((name.contains("lever")) && interactionCategories.contains("levers")) return true;
        if ((name.contains("pressure_plate")) && interactionCategories.contains("pressure_plates")) return true;
        return false;
    }

    private boolean canBasicInteract(Player p, Block b) {
        if (plots.isOwner(p.getUniqueId(), b.getLocation())) return true;
        try {
            // Containers usually require at least "CONTAINER" role
            return plots.hasRoleAtLeast(b.getLocation(), p.getUniqueId(), ClaimRole.CONTAINER);
        } catch (Throwable ignored) {
            return plots.isTrustedOrOwner(p.getUniqueId(), b.getLocation());
        }
    }

    private boolean isTrusted(Player p, Block b) {
        if (plots.isOwner(p.getUniqueId(), b.getLocation())) return true;
        try {
            return plots.hasRoleAtLeast(b.getLocation(), p.getUniqueId(), ClaimRole.MEMBER);
        } catch (Throwable ignored) {
            return plots.isTrustedOrOwner(p.getUniqueId(), b.getLocation());
        }
    }

    /* =========================================================
       Buckets
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("proshield.bypass")) return;
        if (!isInClaim(e.getBlock())) return;
        if (bucketsEmptyBlocked && !hasBuildPermission(p, e.getBlock())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("proshield.bypass")) return;
        if (!isInClaim(e.getBlock())) return;
        if (bucketsFillBlocked && !canBasicInteract(p, e.getBlock())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    /* =========================================================
       Fire control
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (!isInClaim(e.getBlock())) return;

        switch (e.getCause()) {
            case FLINT_AND_STEEL: if (fireIgniteFlint) e.setCancelled(true); break;
            case LAVA:            if (fireIgniteLava) e.setCancelled(true); break;
            case LIGHTNING:       if (fireIgniteLightning) e.setCancelled(true); break;
            case EXPLOSION:       if (fireIgniteExplosion) e.setCancelled(true); break;
            case SPREAD:          if (fireIgniteSpread) e.setCancelled(true); break;
            default: /* nothing */ break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (fireBurn && isInClaim(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        if (fireSpread && e.getSource() != null && e.getSource().getType() == Material.FIRE && isInClaim(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    /* =========================================================
       Explosions
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getLocation() == null) return;
        var loc = e.getLocation();

        // cancel if explosion type blocked & inside a claim
        if (plots.getClaim(loc).isEmpty()) return;

        switch (e.getEntityType()) {
            case CREEPER:       if (explosionCreeper)     e.setCancelled(true); break;
            case PRIMED_TNT:    if (explosionTNT)         e.setCancelled(true); break;
            case WITHER:        if (explosionWither)      e.setCancelled(true); break;
            case WITHER_SKULL:  if (explosionWitherSkull) e.setCancelled(true); break;
            case ENDER_CRYSTAL: if (explosionEndCrystal)  e.setCancelled(true); break;
            case ENDER_DRAGON:  if (explosionDragon)      e.setCancelled(true); break;
            default: /* nothing */ break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        // Generic block explosion in a claim (covers some edge cases)
        if (isInClaim(e.getBlock())) e.setCancelled(true);
    }

    /* =========================================================
       Entity grief (changing blocks)
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (plots.getClaim(e.getBlock().getLocation()).isEmpty()) return;

        switch (e.getEntityType()) {
            case ENDERMAN:
                if (entityGriefEnderman) e.setCancelled(true);
                break;
            case RAVAGER:
                if (entityGriefRavager) e.setCancelled(true);
                break;
            case SILVERFISH:
                if (entityGriefSilverfish) e.setCancelled(true);
                break;
            case ENDER_DRAGON:
                if (entityGriefDragon) e.setCancelled(true);
                break;
            case WITHER:
                if (entityGriefWither) e.setCancelled(true);
                break;
            default:
                // no-op
        }
    }

    /* =========================================================
       Teleport controls
       ========================================================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent e) {
        if (!(e.getEntity() instanceof Enderman)) return;
        if (!endermanTeleportDeny) return;
        if (plots.getClaim(e.getTo()).isPresent()) {
            e.setCancelled(true);
        }
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private boolean isInClaim(Block block) {
        return plots.getClaim(block.getLocation()).isPresent();
    }

    private void deny(Player p) {
        p.sendMessage(prefix() + ChatColor.RED + "Action blocked in this claim.");
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
