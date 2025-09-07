# 🛡️ ProShield

**ProShield** is a lightweight land & plot protection plugin for **Paper/Spigot** (Java 17+).  
It focuses on **simple chunk claims**, a **clean GUI**, and **admin tools** without heavy dependencies.

---

## ✨ Features

- **Chunk Claims**: claim/unclaim the chunk you stand in (1-chunk = 16×16).
- **GUI Menu** (compass): Create Claim • Claim Info • Remove Claim.
- **Protection**:
  - Block break/place denied to non-owners in claimed chunks.
  - Container access control (chests/shulkers, etc.).
  - Interaction control (doors, buttons, plates, levers) with blacklist/whitelist modes.
  - PvP toggle in claims.
  - Explosion control (creeper/TNT/wither/ender crystal/dragon) inside claims.
  - Fire control (spread/burn/ignite sources).
  - Bucket control (place/fill).
  - Entity grief control (enderman/ravager/silverfish/dragon/wither).
  - Enderman teleport denial in claims.
  - **Per-world overrides** for all toggles.
- **Trust System**: trust/untrust players per claim and list trusted.
- **Admin Compass**: craftable & auto-given to OPs on join (if missing).
- **Admin Tools**: bypass mode, reload, expired-claim archival + restore + purge.
- **Expiry**: archive claims for players inactive N days; restore if they return.
- **Owner Toggle**: optionally make `proshield.admin` imply unlimited claims (OFF by default).
- **Shop/API Hooks** (no Vault dep): console/automation commands for shops to integrate.
- **Config-backed**: all settings in `plugins/ProShield/config.yml`. Claims persisted there.

---

## ✅ Requirements

- **Server**: Paper 1.18+ (works on modern Spigot; Paper recommended)
- **Java**: 17+

---

## 📥 Installation

1. Download the latest `proshield-<version>.jar` from Releases.
2. Drop it into `plugins/` and **restart**.
3. `plugins/ProShield/config.yml` will be generated automatically.

---

## 🧭 Admin Compass

**Crafting**
I=Iron Ingot, R=Redstone, C=Compass
- OPs (or players with `proshield.compass`) receive one on join if missing.
- Right-click to open the ProShield GUI.

---

## ⌨️ Commands

`/proshield`  
`/proshield claim` – claim current chunk  
`/proshield unclaim` – unclaim current chunk (owner only)  
`/proshield info` – show owner & trusted of current chunk  
`/proshield compass` – give yourself the compass (perm required)  
`/proshield trust <player>` – trust a player in your current claim  
`/proshield untrust <player>` – remove trust  
`/proshield trusted` – list trusted players in current claim  
`/proshield bypass <on|off|toggle>` – admin build bypass  
`/proshield reload` – reload config + per-world protection  
`/proshield expired list` – list archived claims  
`/proshield expired restore <world:cx:cz>` – restore archived claim  
`/proshield expired purge [days]` – purge archived claims (all or older than N days)  
`/proshield settings adminUnlimited <on|off|toggle>` – owner-only switch  
`/proshield api givecompass <player>` – shop hook: give compass  
`/proshield api expand <player> <amount>` – shop hook: queue expansion (placeholder)  
`/proshield api grant <player> <permission>` – shop hook: forward to perms plugin (LuckPerms)

---

## 🔐 Permissions

- `proshield.use` – use player commands & GUI (**default: true**)
- `proshield.compass` – give/use compass (**default: op**)
- `proshield.bypass` – toggle bypass (**default: op**)
- `proshield.unlimited` – ignore max-claims (**default: false**)
- `proshield.admin.tp` – admin teleport in future GUI (**default: op**)
- `proshield.admin.reload` – `/proshield reload` (**default: op**)
- `proshield.admin.expired.list` – list expired (**default: op**)  
- `proshield.admin.expired.restore` – restore expired (**default: op**)  
- `proshield.admin.expired.purge` – purge expired (**default: op**)
- `proshield.admin` – umbrella admin (**default: op**) *(does **not** include unlimited)*
- `proshield.owner` – owner-only switches (e.g., adminIncludesUnlimited) (**default: false**)
- API hooks (for shops/automation; **default: false**):
  - `proshield.api.givecompass`
  - `proshield.api.expand`
  - `proshield.api.grant`

api:
  enable-command-hooks: true        # enable /proshield api ... commands

protection:
  containers: true
  pvp-in-claims: false
  mob-grief: true
  creeper-explosions: true
  tnt-explosions: true
  wither-explosions: true
  wither-skull-explosions: true
  ender-crystal-explosions: true
  ender-dragon-explosions: true
  fire:
    spread: true
    burn: true
    ignite:
      flint_and_steel: true
      lava: true
      lightning: true
      explosion: true
      spread: true
  interactions:
    enabled: true
    mode: blacklist                 # or whitelist
    categories: [doors, trapdoors, fence_gates, buttons, levers, pressure_plates]
    list: []                        # explicit material names
  buckets:
    block-empty: true
    block-fill: true
  entity-grief:
    enderman: true
    ravager: true
    silverfish: true
    ender-dragon: true
    wither: true
  entity-teleport:
    enderman: true

autogive:
  compass-on-join: true


---

🧱 Data

Claims are saved to config.yml under claims:.

Expired/archived claims are saved under claims_expired: with metadata for restore/purge.



