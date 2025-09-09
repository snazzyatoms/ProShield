# 🛡️ ProShield 2.0 Roadmap

> **Note:** Several roadmap features have already been **introduced in 1.2.x**:  
> - ✅ Claim Roles (Visitor, Member, Container, Builder, Co-Owner)  
> - ✅ GUI trust management (slots, back button, role assignment polish)  
> - ✅ Claim Item Protection (toggle for keeping dropped items inside claims)  
> - ✅ Claim Preview & Ownership Transfer tools  
> - ✅ Admin Compass (auto-given to OPs)  
> - ✅ Spawn Guard radius (configurable)  
> - ✅ Mob Border Repel (hostiles bounce off claim edges)  
> - ✅ Config reload via command **and GUI button**  
>
> ProShield 2.0 will **expand and refine** these systems and introduce **Towns, Shops, Claim Flags, Entry/Exit Messages, and Economy integration**.

---

## ✨ Vision
- Lightweight and performance-friendly.
- Simple for new players, powerful for admins.
- Flexible enough to integrate with towns, shops, and server economies.
- Modular: server owners enable only what they need.

---

## 🌍 Multi-World & Global Controls
- Per-world configuration files (different rules per world).
- Server-wide global claim defaults.
- Protected "no-claim" zones (e.g., spawn or hub).
- **Next:** Dynamic per-world overrides through GUI (not only YAML).

---

## 🏘️ Towns & Communities
- **Merge claims** into Towns/Communities.
- Town ranks:
  - Mayor (founder/owner)
  - Assistants (manage claims, trust players)
  - Residents (trusted members)
- Town-wide permissions and flags.
- Commands:
  - `/proshield town create <name>`
  - `/proshield town invite <player>`
  - `/proshield town promote <player>`
- **Next:** Town GUI dashboard for managing claims, members, and flags.

---

## 💰 Economy Integration
- Optional claim purchase cost (Vault or economy plugin required).
- Upkeep/maintenance system: inactive or unpaid claims auto-expire.
- Integration with shop plugins:
  - Auto-protect shops inside claims.
  - Role-based shop management.
- **Next:** Town bank & upkeep system tied into Vault.

---

## 🏷️ Claim Flags (Per-Claim Toggles)
- Flags configurable per claim:
  - PvP (on/off)
  - Explosions
  - Fire spread
  - Animal interactions
  - Redstone mechanics
- GUI-based flag editor.
- Configurable server-wide defaults.
- **Next:** Editable defaults per world.

---

## 📢 Player Experience & QoL
- Claim entry/exit messages (customizable per claim).
- `/proshield map` → show nearby claims (text-grid or minimap-style).
- Visualization mode:
  - Temporary glowing borders
  - Particle outlines when claiming land
- **Next:** Compass integration with teleport shortcuts.

---

## 🔐 Advanced Roles & Access
- Expand roles from 1.2:
  - Visitor (no interaction)
  - Member (basic interaction/build)
  - Container (chests/furnaces only)
  - Manager (invite/trust others, toggle flags)
  - Co-Owner (full control)
- Editable roles per claim or town.
- GUI role manager for ease of use.
- **Next:** Per-claim **custom role editor** for advanced servers.

---

## ⚙️ Admin Tools & Maintenance
- Expired claim auto-cleanup:
  - **Preview mode** before deletion.
- `/proshield stats` → claim statistics and summaries.
- Force-merge or force-claim abandoned areas.
- Optional "read-only mode" for maintenance.
- **Next:** In-game **admin audit log** for claim actions.

---

## 🔮 Future-Proofing
- Plugin API hooks for developers:
  - Shop protection
  - Quests
  - Events
- Modular architecture:
  - Towns, economy, claims → toggleable in config.
- Exploration of **web dashboard** integration (long-term).
- Potential cache-based optimizations for **large-scale SMPs**.

---

## ✅ Goals for 2.0
- Make ProShield the **go-to lightweight protection plugin** for survival servers.
- Offer **community and economy integration** without sacrificing performance.
- Deliver a polished, stable release that sets the foundation for long-term growth.

---

💡 **Want to contribute ideas?**  
Open a [GitHub Issue](https://github.com/snazzyatoms/ProShield/issues) or join the Spigot discussion thread!
