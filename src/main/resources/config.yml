# ==========================================================
# ProShield Configuration
# Version: 1.2.5 (Finalized for release build)
# ==========================================================
# Notes:
#   - Use '&' for colors
#   - Placeholders supported in messages:
#       {player}, {claim}, {role}, {owner}, {state}, {count}
# ==========================================================

# ----------------------------------------------------------
# General Settings
# ----------------------------------------------------------
settings:
  give-compass-on-join: true

# ----------------------------------------------------------
# Protection Settings
# ----------------------------------------------------------
protection:
  fire:
    enabled: false
    burn: false
    spread: false
    ignite:
      flint_and_steel: false
      lava: false
      lightning: false

  explosions:
    enabled: false

  buckets:
    enabled: false

  mobs:
    border-repel:
      enabled: true
      radius: 3.0           # distance mobs are repelled at claim edge
      horizontal-push: 0.7  # sideways push
      vertical-push: 0.25   # upward push
    despawn-inside: true     # mobs instantly removed if inside a claim

  entities:
    wilderness:
      item-frames: false
      vehicles: false

# ----------------------------------------------------------
# Messages
# ----------------------------------------------------------
messages:
  prefix: "&3[ProShield]&r "
  debug-prefix: "&8[Debug]&r "
  show-wilderness: true
  admin-flag-chat: true

  # Claim entry/exit messages
  enter-own: "&aYou entered your claim."
  enter-other: "&aYou entered {owner}'s claim."
  leave-own: "&cYou left your claim."
  leave-other: "&cYou left {owner}'s claim."
  wilderness: "&7You entered the wilderness."

# ----------------------------------------------------------
# Help Messages
# ----------------------------------------------------------
help:
  player:
    - "&bProShield Help &7- Player"
    - "&a/claim &7→ Claim your current chunk"
    - "&a/unclaim &7→ Unclaim your current chunk"
    - "&a/proshield compass &7→ Receive your ProShield Compass"
    - "&a/proshield help &7→ Show this help menu"

  admin:
    - "&cProShield Help &7- Admin"
    - "&c/proshield reload &7→ Reload the config"
    - "&c/proshield debug &7→ Toggle debug mode"
    - "&c/proshield bypass &7→ Toggle bypass mode"
    - "&c/proshield admin &7→ Open the admin menu"

# ----------------------------------------------------------
# GUI Menus (Config-driven)
# ----------------------------------------------------------
gui:
  menus:

    main:
      title: "&bProShield Menu"
      size: 27
      items:
        "10":
          material: GRASS_BLOCK
          name: "&aClaim Chunk"
          lore:
            - "&7Claim your current chunk"
          action: "command:claim"
        "11":
          material: BARRIER
          name: "&cUnclaim Chunk"
          lore:
            - "&7Unclaim your current chunk"
          action: "command:unclaim"
        "12":
          material: PAPER
          name: "&eClaim Info"
          action: "command:proshield info"
        "13":
          material: PLAYER_HEAD
          name: "&aTrust Menu"
          action: "menu:trust"
        "14":
          material: SKELETON_SKULL
          name: "&cUntrust Menu"
          action: "menu:untrust"
        "15":
          material: BOOK
          name: "&6Roles"
          action: "menu:roles"
        "16":
          material: IRON_SWORD
          name: "&cFlags"
          action: "menu:flags"

        # --- Admin-only entries (require permission) ---
        "20":
          material: PLAYER_HEAD
          name: "&aTrust Players (Admin)"
          action: "menu:trust"
          permission: "proshield.admin"
        "21":
          material: BOOK
          name: "&6Roles (Admin)"
          action: "menu:roles"
          permission: "proshield.admin"
        "22":
          material: IRON_SWORD
          name: "&cFlags (Admin)"
          action: "menu:flags"
          permission: "proshield.admin"

        "26":
          material: BARRIER
          name: "&cClose"
          action: "close"

    trust:
      title: "&aTrust Player"
      size: 27
      items:
        "13":
          material: PLAYER_HEAD
          name: "&aEnter Player"
          action: "command:trust"
        "26":
          material: BARRIER
          name: "&cBack"
          action: "menu:main"

    untrust:
      title: "&cUntrust Player"
      size: 27
      items:
        "13":
          material: SKELETON_SKULL
          name: "&cEnter Player"
          action: "command:untrust"
        "26":
          material: BARRIER
          name: "&cBack"
          action: "menu:main"

    flags:
      title: "&dClaim Flags"
      size: 27
      items:
        "10":
          material: TNT
          name: "&cExplosions"
          lore:
            - "&7Toggle TNT and creeper damage inside claim"
            - "&7Current: {state}"
          action: "command:proshield flag explosions"

        "11":
          material: WATER_BUCKET
          name: "&bBuckets"
          lore:
            - "&7Allow/disallow bucket use inside claim"
            - "&7Covers water & lava placement"
            - "&7Current: {state}"
          action: "command:proshield flag buckets"

        "12":
          material: ITEM_FRAME
          name: "&6Item Frames"
          lore:
            - "&7Protect item frames from breaking/rotation"
            - "&7Current: {state}"
          action: "command:proshield flag item-frames"

        "13":
          material: ARMOR_STAND
          name: "&eArmor Stands"
          lore:
            - "&7Prevent others from moving/destroying armor stands"
            - "&7Current: {state}"
          action: "command:proshield flag armor-stands"

        "14":
          material: CHEST
          name: "&aContainers"
          lore:
            - "&7Allow or block access to chests, hoppers, furnaces, shulkers"
            - "&7Current: {state}"
          action: "command:proshield flag containers"

        "15":
          material: BONE
          name: "&dPets"
          lore:
            - "&7Prevent damage to wolves, cats, and other tamed pets"
            - "&7Current: {state}"
          action: "command:proshield flag pets"

        "16":
          material: IRON_SWORD
          name: "&cPvP"
          lore:
            - "&7Enable or disable player-vs-player combat inside claim"
            - "&7Current: {state}"
          action: "command:proshield flag pvp"

        "17":
          material: SHIELD
          name: "&aSafe Zone"
          lore:
            - "&7Turns your claim into a safe zone"
            - "&7Blocks hostile spawns and damage"
            - "&7Current: {state}"
          action: "command:proshield flag safezone"

        "26":
          material: BARRIER
          name: "&cBack"
          action: "menu:main"

# ----------------------------------------------------------
# Sounds
# ----------------------------------------------------------
sounds:
  button-click: "UI_BUTTON_CLICK"
  claim-success: "ENTITY_PLAYER_LEVELUP"
  unclaim-success: "ENTITY_ITEM_BREAK"
  flag-toggle: "BLOCK_NOTE_BLOCK_PLING"
  admin-action: "ENTITY_EXPERIENCE_ORB_PICKUP"

# ----------------------------------------------------------
# Claim Defaults
# ----------------------------------------------------------
claims:
  default-flags:
    pvp: false
    explosions: false
    fire: false
    entity-grief: false
    redstone: true
    containers: false
    animals: false
    vehicles: false
    armor-stands: false
    item-frames: false
    buckets: false
    pets: false
    mob-repel: true
    mob-despawn: true
    safezone: true   # ✅ claims start as safe zones by default
