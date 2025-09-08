# 📜 ProShield Changelog

This file documents all notable changes to **ProShield**, starting from the first stable release.

---

## **1.2.2 — Item Keep & GUI Enhancements**
Released: 2025-09-08

### 🔑 New Features
- **Keep Items in Claims (NEW)**  
  - Items dropped inside claims can optionally be protected from despawning.  
  - Configurable in `config.yml` under `claims.keep-items`.  
  - Default: **off** (server owners must enable it).  
  - Adjustable despawn delay: default `900s` (15 min), configurable between `300–900s`.  
  - Admin GUI toggle available (requires `proshield.admin.keepdrops`).  

- **GUI Enhancements**
  - Added a **Help slot (31)** → Shows only commands available to the player’s permissions/roles.  
  - Wired **Admin slot (33)** → Opens the admin GUI instead of placeholder text.  
  - More polished layout for clarity.  

- **Config Improvements**
  - Added version header: now shows **1.2.2** on fresh installs.  
  - Clearer comments and grouping for protection toggles and claim options.  
  - Automatic migration snippet ensures `claims.keep-items` is added if missing.  

### 🛠 Fixes & Improvements
- Fixed constructor mismatch between `ProShield.java` and `GUIManager.java`.  
- General cleanup of listeners and event checks.  
- Minor performance improvements when handling trusted players and role lookups.  
- Compass distribution logic further hardened to prevent edge-case duplication.  

---

## **1.2.1 — Roles & Quality of Life**
Released: 2025-09-08

### 🔑 New Features
- **Claim Roles (NEW)**  
  - Players can now assign roles when trusting others:  
    - `Visitor` → Can enter claims but cannot interact.  
    - `Member` → Can interact with doors, buttons, containers.  
    - `Builder` → Can build and break blocks.  
    - `Co-Owner` → Nearly full permissions, but not the original owner.  
  - `/proshield trust <player> [role]` supports role assignment.  
  - GUI updated to allow role selection for trusted players.  

- **GUI Enhancements**
  - Radius-based trust: Easily add nearby players to your claim from the GUI.  
  - Cleaner role/permissions interface for managing trusted players.  

- **Admin Tools**
  - Expanded `/proshield purgeexpired <days> [dryrun]` to support preview mode.  
  - Admins can now view and manage roles inside claims.  

### 🛠 Fixes & Improvements
- Fixed **compass duplication issue** on join.  
- Improved claim protection logic for bucket usage and entity grief events.  
- Config reload (`/proshield reload`) now fully refreshes protection, roles, and claim rules.  
- Minor performance optimizations for claim lookups and expiry cleanup.  

---

## **1.1.9 — Quality of Life & Expanded Controls**
Released: 2025-09-07

### 🔑 New Features
- **Trust System**
  - `/proshield trust <player>` – allow another player to build in your claim.  
  - `/proshield untrust <player>` – revoke access.  
  - `/proshield trusted` – list trusted players.

- **Protection Expansion**
  - **Interactions**: blacklist/whitelist for doors, trapdoors, gates, buttons, levers, pressure plates, etc.  
  - **Explosions**: control creepers, TNT, withers, wither skulls, ender crystals, ender dragons.  
  - **Fire**: spread, ignite (flint & steel, lava, lightning), and burn.  
  - **Buckets**: toggle filling or emptying in claimed land.  
  - **Entity grief**: control Enderman, Ravagers, Silverfish, Ender Dragon, Wither grief.  
  - **Enderman teleport denial** inside claims.  
  - **Per-world overrides** for all of the above.

- **Claim Expiry**
  - Config option: expire claims after N days of inactivity.  
  - `/proshield expired list` – view expired claims.  
  - `/proshield expired restore <key>` – restore archived claim.  
  - `/proshield expired purge [days]` – purge expired claims.  
  - Expired claims moved to `claims_expired:` in config for safety.

- **Admin Features**
  - `/proshield bypass <on|off|toggle>` – toggle protection bypass.  
  - `/proshield reload` – reload configs and recache protection rules.  
  - Admin-only toggle: `/proshield settings adminUnlimited <on|off|toggle>` (guarded by `proshield.owner`).  
  - New permissions:
    - `proshield.admin.expired` – manage expired claims.  
    - `proshield.admin.settings` – change ProShield owner-level settings.

- **Compass Improvements**
  - Given automatically to OPs/admins with permission on join if missing.  
  - Right-click opens the ProShield GUI.  
  - Craftable with custom recipe (iron + redstone + compass).

---

## **1.1.8 — Stability & Polish**
Released: 2025-09-06

### 🛠 Fixes & Improvements
- Fixed GUI title checks (avoiding `getTitle()` issues).  
- Corrected listener wiring (GUI, compass, claims).  
- Join-compass logic improved:
  - Ensures only eligible OPs/admins receive compass.  
  - Prevents duplicate compasses in inventory.  
- Build improvements:
  - POM and JitPack locked to Java 17.
  - Paper API properly configured as `provided`.  
  - Clean and stable build artifacts.

---

## **1.1.7 — First Stable Public Build**
Released: 2025-09-05

### ✨ Features
- **Claims**
  - Players can claim chunks using `/proshield claim`.  
  - Unclaim land with `/proshield unclaim`.  
  - View claim info with `/proshield info`.
