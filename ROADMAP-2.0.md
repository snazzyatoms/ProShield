# 🛡️ ProShield 2.0 Roadmap

[![SpigotMC](https://img.shields.io/badge/Spigot-Paper-blue)]() 
[![Minecraft](https://img.shields.io/badge/MC-1.18--1.21-green)]() 
[![Multilingual](https://img.shields.io/badge/🌐-Multilingual-lightblue)]() 
[![GUI-First](https://img.shields.io/badge/🖥️-GUI--First-orange)]() 
[![Lightweight](https://img.shields.io/badge/⚡-Lightweight-yellow)]() 

---

## 🌟 Highlights

**Already in v1.2.6:**  
- ✅ Claim Roles (Visitor → Co-Owner)  
- ✅ Trusted Players GUI with **roles + back/exit navigation**  
- ✅ Claim Transfer & Claim Preview tools  
- ✅ Admin Compass (auto-given if enabled, unified in 1.2.6)  
- ✅ Spawn Guard Radius (anti-claim near spawn)  
- ✅ Mob Border Repel + Safezone Controls (repel, despawn, path-block)  
- ✅ Expansion Requests (player GUI → admin review/deny/history)  
- ✅ World Controls GUI (fire, explosions, mobs, trample toggle)  
- ✅ Safezones (full protection zones)  
- ✅ Admin GUI (reload, debug, bypass, purge expired claims)  
- ✅ Multilingual Support (all text in `messages.yml`, fully editable by server owners)  
- ✅ Crop Trample flag (toggle via GUI)  
- ✅ Polished Compass Sync (GUIListener + CompassManager unified)  
- ✅ GUI Fixes (Admin Tools submenus now open & functional)  

**Coming in 2.0:**  
- 🏘️ Towns & Communities (mayors, assistants, residents)  
- 💰 Economy Integration (Vault support, upkeep, town banks)  
- 🏷️ Advanced Claim Flags (per-world defaults, custom toggles)  
- 📢 Entry/Exit Messages & Claim Map (/proshield map)  
- 🔐 Custom Role Editor (define unique roles per claim/town)  
- ⚙️ Admin Audit Logs (track claim/expansion actions)  
- 🌐 Bungee / Proxy Support (multi-server claim + trust sync)  
- 🔮 Developer API + Long-Term Features (shops, quests, web dashboard)  

---

## ✨ Vision
- **Lightweight & performance-friendly** — ideal for SMP servers.  
- **GUI-first experience** — commands exist only as fallbacks.  
- **Fully multilingual** — server owners can polish or translate all text via `messages.yml`.  
- **Future-ready** — modular towns, economy hooks, and proxy support.  

---

## 🌍 Multi-World & Global Controls
- ✅ Per-world safezones and global claim defaults.  
- ✅ World Controls GUI for toggling protections live.  
- ✅ Nether/End placeholders reserved in 1.2.6.  
- **Next:** GUI-based per-world editors for full defaults and overrides.  

---

## 🏘️ Towns & Communities
- Merge individual claims into larger **Towns**.  
- Town ranks: Mayor, Assistants, Residents.  
- Town-wide flags, shared banks, and permissions.  
- Commands:  
  - `/proshield town create <name>`  
  - `/proshield town invite <player>`  
  - `/proshield town promote <player>`  
- **Next:** Full **Town GUI dashboard** for members, claims, finances.  

---

## 💰 Economy Integration
- Optional **claim purchase costs** (Vault-compatible).  
- Claim/town **upkeep system**: unpaid claims/towns expire.  
- Shop plugins integration:  
  - Auto-protect shops within claims.  
  - Role-based shop access.  
- **Next:** Town banks & shared upkeep tied into Vault.  

---

## 🏷️ Claim Flags (Per-Claim Toggles)
- ✅ Player-level **Flags GUI** (PvP, fire, explosions, mobs, crop trample).  
- Configurable defaults in `config.yml`.  
- **Next:** Editable defaults per world with inheritance.  

---

## 📢 Player Experience & QoL
- ✅ Back & Exit buttons consistent across all GUIs.  
- ✅ Expansion Requests & History menus.  
- ✅ Compass Sync polished in 1.2.6.  
- Claim entry/exit messages (planned in 2.0).  
- `/proshield map` → show nearby claims (grid/minimap).  
- **Next:** Compass teleport integration & quick-travel tools.  

---

## 🔐 Advanced Roles & Access
- Roles from Visitor → Manager → Co-Owner.  
- GUI-driven **Trusted Players menu**.  
- **Next:** **Custom Role Editor** for unique, per-claim permissions.  

---

## ⚙️ Admin Tools & Maintenance
- ✅ Admin Tools GUI with reload/debug/bypass.  
- ✅ Expansion Review & History GUIs.  
- ✅ World Controls GUI.  
- Expired claim cleanup + purge tools.  
- `/proshield stats` → claim statistics.  
- Force-merge or unclaim abandoned areas.  
- **Next:** In-game **admin audit logs**.  

---

## 🔮 Future-Proofing
- Developer API hooks: shops, quests, integrations.  
- Modular architecture → towns, economy, shops toggleable.  
- Web dashboard potential (long-term).  
- Proxy/Bungee support for large multi-server networks.  
- Optimized for large SMPs (async tasks, caching).  

---

## ✅ Goals for 2.0
- Solidify ProShield as the **go-to lightweight protection plugin**.  
- Deliver **towns, shops, and economy integration** without bloat.  
- Keep **multilingual + GUI-first** philosophy at the core.  
- Support **SMP and proxy networks** with stability and performance.  

---

💡 **Got ideas?**  
Open a [GitHub Issue](https://github.com/snazzyatoms/ProShield/issues) or join the **Spigot discussion thread**.
