# ==========================================================
# ProShield Messages
# Version: 1.2.5
# Synced with GUIManager + MessagesUtil + config.yml
# ==========================================================

messages:
  prefix: "&3[ProShield]&r "
  debug-prefix: "&8[Debug]&r "

  # --- General Errors ---
  error:
    player-only: "&cOnly players can use this action."
    no-permission: "&cYou do not have permission to do this."

  # --- Compass ---
  compass:
    command-success: "&aA ProShield Compass has been given to you."
    already-have: "&eYou already have a ProShield Compass."

  # --- Admin / System ---
  reloaded: "&aProShield configuration reloaded."

  admin:
    debug-on: "&eDebug mode: &aENABLED"
    debug-off: "&eDebug mode: &cDISABLED"
    bypass-on: "&aBypass enabled."
    bypass-off: "&cBypass disabled."

  # --- Claim / Region Transitions ---
  show-wilderness: true
  admin-flag-chat: true

  enter-own: "&aYou entered your claim."
  enter-other: "&aYou entered {owner}'s claim."
  leave-own: "&cYou left your claim."
  leave-other: "&cYou left {owner}'s claim."
  wilderness: "&7You entered the wilderness."

  # --- Expansion Requests ---
  expansion-request: "&eYour expansion request for +{blocks} blocks has been sent to admins."
  expansion-approved: "&aYour claim expansion (+{blocks} blocks) was approved!"
  expansion-denied: "&cYour expansion request was denied: {reason}"

  expansion-disabled: "&cExpansion requests are disabled by the server."
  expansion-cooldown-title: "&cCooldown Active"
  expansion-cooldown-active: "&7You can request again in &f{hours}h {minutes}m&7."

  # Admin review GUI tooltips
  expansion-admin-lore:
    approve: "&aLeft-click: Approve this expansion"
    deny: "&cRight-click: Deny and choose a reason"
    info: "&7Use the deny reasons menu for detailed denial messages."

  # Deny reasons (used in Admin GUI → Deny menu)
  deny-reasons:
    too-large: "&cRequested expansion is too large."
    abusive: "&cExpansion request denied due to abuse."
    overlap: "&cRequested area overlaps an existing claim."
    custom-1: "&cNot enough playtime yet for this expansion."
    custom-2: "&cYour claim already reached the maximum size."

# ==========================================================
# Help Pages (GUI-first style)
# ==========================================================
help:
  player:
    - "&3&lProShield &7— Player Guide"
    - "&aRight-click your ProShield Compass &7→ Open the main menu"
    - "&bMain Menu &7→ Claim Land, Info, Unclaim, Trusted Players, Claim Flags"
    - "&bTrusted Players &7→ Manage roles & access"
    - "&bClaim Flags &7→ Toggle protections"
    - "&aRequest Expansion &7→ Ask admins to expand your claim (if enabled)"
    - "&7Expansion requests have a cooldown of &f{cooldown-hours}h &7(default)."
    - "&7Note: All features are GUI-driven. Commands are not required."

  admin:
    - "&c&lProShield — Admin Guide"
    - "&cAdmin Tools GUI &7→ Reload, Debug, Bypass"
    - "&7Requires: &fproshield.admin"
    - "&7Expansion Requests and World Controls require higher permissions."
    - "&7Available Commands (for admins only):"
    - "&f/proshield reload &7→ Reload configs"
    - "&f/proshield debug &7→ Toggle debug mode"
    - "&f/proshield bypass &7→ Toggle admin bypass"
    - "&f/proshield admin &7→ Open Admin Tools GUI"

  senior:
    - "&4&lSenior Admin Tools"
    - "&4Expansion Requests &7– Requires &fproshield.admin.expansions"
    - "&4World Controls &7– Requires &fproshield.admin.worldcontrols"
