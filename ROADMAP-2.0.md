# 🛡️ ProShield 2.0 Roadmap

## 🌟 Highlights

**Already in 1.2.5:**  
- ✅ Claim Roles (Visitor → Co-Owner)  
- ✅ GUI Trust Management (roles, back/exit buttons)  
- ✅ Item Protection (toggle keep-drops in claims)  
- ✅ Claim Transfer & Preview Tools  
- ✅ Admin Compass (auto-given to OPs)  
- ✅ Spawn Guard Radius (anti-claim near spawn)  
- ✅ Mob Border Repel + Safezone Controls (repel, despawn, path-block)  
- ✅ Expansion Requests (player GUI → admin review/deny/history)  
- ✅ World Controls GUI (fire, explosions, mobs, etc.)  
- ✅ Safezones (full protection zones)  
- ✅ Admin GUI (reload, debug, bypass, purge expired claims)  

**Coming in 2.0:**  
- 🏘️ Towns & Communities (mayor, assistants, residents)  
- 💰 Economy Integration (Vault support, upkeep, town banks)  
- 🏷️ Advanced Claim Flags (per-world defaults, custom toggles)  
- 📢 Entry/Exit Messages & Claim Map (/proshield map)  
- 🔐 Custom Role Editor (define unique roles per claim/town)  
- ⚙️ Admin Audit Logs (track claim/expansion actions)  
- 🔮 Developer API + Long-Term Features (shops, quests, web dashboard)  

---

> **Note:** ProShield 1.2.x already introduced **roles, flags, expansion requests, safezones, mob repel, and admin tools**.  
> ProShield 2.0 builds on these foundations to deliver **Towns, Shops, Economy, Custom Roles, and Global Controls**.

---

## ✨ Vision
- **Lightweight & performance-friendly** — perfect for survival SMPs.  
- **Simple for players**, yet **powerful for admins**.  
- **Modular & flexible** — enable only what your server needs.  
- Build towards **community features** (towns, shops, economies).  

---

## 🌍 Multi-World & Global Controls
- ✅ Per-world safezones and global claim defaults.  
- ✅ World Controls GUI for toggling protections live.  
- Per-world YAML overrides for advanced configs.  
- **Next:** GUI-based per-world editors (editable defaults per world).  

---

## 🏘️ Towns & Communities
- **Merge individual claims** into larger **Towns**.  
- Town ranks:  
  - Mayor (founder/owner)  
  - Assistants (manage claims, invite/trust players)  
  - Residents (trusted members)  
- Town-wide permissions and shared flags.  
- Commands:  
  - `/proshield town create <name>`  
  - `/proshield town invite <player>`  
  - `/proshield town promote <player>`  
- **Next:** Full **Town GUI dashboard** for managing members, claims, and finances.  

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
- ✅ Player-level **Claim Flags GUI** (since 1.2.5).  
- Configurable per-claim toggles:  
  - PvP  
  - Explosions  
  - Fire spread  
  - Animal interactions  
  - Redstone mechanics  
- Server-wide defaults in `config.yml`.  
- **Next:** **Editable defaults per world** with inheritance.  

---

## 📢 Player Experience & QoL
- ✅ **Back & Exit buttons** across all GUIs (since 1.2.5).  
- ✅ **Expansion Requests menu** for players.  
- ✅ **Expansion History with pagination** for admins.  
- Claim entry/exit messages (planned in 2.0).  
- `/proshield map` → show nearby claims (grid or minimap-style).  
- Visualization mode: glowing borders & particles.  
- **Next:** Compass teleport integration + quick-travel tools.  

---

## 🔐 Advanced Roles & Access
- Expanded roles beyond 1.2.x:  
  - Visitor → walk only  
  - Member → basic interaction  
  - Container → chest/furnace use  
  - Builder → full build/break  
  - Manager → invite/trust others, toggle flags  
  - Co-Owner → near full access  
- Editable roles per-claim or per-town.  
- GUI-driven role editor.  
- **Next:** **Custom role editor** for servers needing fine-grained access control.  

---

## ⚙️ Admin Tools & Maintenance
- ✅ **Admin Tools GUI** with reload/debug/bypass (1.2.4+)  
- ✅ **Expansion Review GUI** with deny reasons (1.2.5)  
- ✅ **Expansion History GUI** with pagination (1.2.5)  
- ✅ **World Controls GUI** to toggle global protections (1.2.5)  
- Expired claim auto-cleanup (configurable, with purge tools).  
- `/proshield stats` → claim statistics & summaries.  
- Force-merge or unclaim abandoned areas.  
- **Next:** In-game **admin audit log** (track actions/approvals).  

---

## 🔮 Future-Proofing
- Plugin API hooks for devs:  
  - Shop protection  
  - Quest/event integration  
- Modular architecture:  
  - Towns, shops, economy, claims → all toggleable.  
- Potential **web dashboard** integration (long-term).  
- Optimizations for **large SMPs** (caching, async tasks).  

---

## ✅ Goals for 2.0
- Establish ProShield as the **go-to lightweight protection plugin**.  
- Deliver **community + economy integration** while keeping performance first.  
- Provide a **stable foundation** for long-term SMP servers.  

---

💡 **Want to contribute ideas?**  
Open a [GitHub Issue](https://github.com/snazzyatoms/ProShield/issues) or join the **Spigot discussion thread**!
