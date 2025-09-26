# 🛡️ ProShield
*A lightweight, polished land & plot protection plugin for Spigot & Paper servers.*

> Perfect for **Survival** or **SMP communities**, ProShield provides simple claiming, intuitive GUIs, and powerful admin tools — without the heavy bloat of larger region plugins.

---

## ✨ Features

- 🏡 **Land Claiming** – Protect chunks with one command or the ProShield Compass GUI.  
- ❌ **Unclaiming** – Release land instantly when no longer needed.  
- 🔒 **Protection System** – Prevent griefing: block break/place, containers, interactions, fire spread, TNT/creeper explosions, entity grief, and more.  
- 👥 **Trust System** – To allow friends access.  
- 🎭 **Claim Roles** *(since 1.2.1)* – Define access levels: Visitor, Member, Builder, Container, Moderator, Manager, Owner.  
- 🎒 **Keep Items in Claims** *(1.2.2+)* – Prevent items dropped in claims from despawning (configurable).  
- 🔄 **Ownership Transfer** *(1.2.3)* – Owners can transfer claims to another player.  
- ✨ **Claim Borders Preview** *(1.2.3)* – Visualize boundaries before confirming.  
- 🖥️ **GUI Menus** – Clean & intuitive for claims, flags, roles, expansions, and admin tools.  
- ⏪ **Back & Exit Buttons** *(improved 1.2.5)* – Consistent across all menus.  
- 🧭 **ProShield Compass** – Right-click to open GUI (auto-given if enabled).  
- 🕒 **Claim Expiry** – Auto-remove claims from inactive players (optional).  
- ⚡ **Configurable Protections** – Containers, doors, buttons, buckets, mobs, explosions, fire, Enderman teleport.  
- 🛑 **Spawn Guard** *(1.2.4)* – Block claiming around spawn radius.  
- 🧟 **Mob Control** *(enhanced 1.2.5)* – Repel mobs, despawn hostiles, block targeting in safezones.  
- 📈 **Expansion Requests** *(1.2.5)* – Players request expansions, admins approve/deny via GUI.  
- 📜 **Expansion History** *(1.2.5)* – Paginated GUI to view past requests.  
- 🌍 **World Controls** *(1.2.5)* – Admin GUI toggles global protections (fire, explosions, mobs).  
- 🔧 **Reload & Debug Tools** *(improved 1.2.5)* – Reload configs/messages via GUI or command.  
- 🎨 **Refined GUIs** *(1.2.6)* –  
  - Removed placeholder `#` tags.  
  - Synced titles with `messages.yml`.  
  - Back/Exit buttons now consistent.  
- 🧭 **Compass Sync** *(1.2.6)* – Unified CompassManager + GUIListener.  
- 🌾 **New Claim Flag** *(1.2.6)* – Crop Trample toggle.  
- 🪐 **World Placeholders** *(1.2.6)* – Nether & End reserved in GUI.  
- 🛠️ **Bug Fixes** *(1.2.6)* – Fixed Admin Tools submenus (Reload, World Controls, Expansion Requests).  

---

## ⚠️ Migration Notes (→ v1.2.6)

If upgrading from **v1.2.5 or earlier**, you **must regenerate your configs** to sync new features.

**What changed:**  
- Back/Exit buttons finalized  
- Nether/End placeholders in world controls  
- Crop Trample flag added  
- Expansion requests cooldown/expiry  

**Steps:**  
1. ⛔ Stop your server  
2. 📂 Backup + delete `/plugins/ProShield/`  
3. ▶️ Restart → new configs generated  
4. 📝 Reapply custom changes  

---

## 🏡 Getting Started

### 🔹 Option 1: ProShield Compass (GUI)
- Given automatically on join *(if enabled)*.  
- Or via `/proshield compass`.  
- **Right-click** to open menu:  
  - Slot 11 → Claim land  
  - Slot 13 → Claim Info  
  - Slot 15 → Unclaim  
  - Slot 16 → Trusted Players (roles)  
  - Slot 28 → Claim Flags  
  - Slot 30 → Request Expansion  
  - Slot 32 → Admin Tools *(if permissioned)*  

### 🔹 Option 2: Commands
- `/proshield claim` – Claim current chunk  
- `/proshield unclaim` – Remove your claim  
- `/proshield info` – View claim info  
- `/proshield trust <player> [role]` – Grant access  
- `/proshield untrust <player>` – Remove trust  
- `/proshield trusted` – List trusted players  

💡 **Tip:** Compass GUI = simplicity. Commands = precision.

---

## 🔑 Permissions

| Node                             | Description                                     | Default |
|----------------------------------|-------------------------------------------------|---------|
| `proshield.player.access`        | Core player access (claims, compass, GUI)       | ✅ true |
| `proshield.compass`              | Receive/use compass                             | ✅ true |
| `proshield.player.claim`         | Create/manage own claims                        | ✅ true |
| `proshield.unlimited`            | Ignore claim limits                             | ❌ op   |
| `proshield.admin`                | Access admin tools GUI & commands               | ❌ op   |
| `proshield.admin.reload`         | Reload configs via command/GUI                  | ❌ op   |
| `proshield.admin.debug`          | Toggle debug logging                            | ❌ op   |
| `proshield.admin.expansions`     | Approve/Deny expansion requests                 | ❌ op   |
| `proshield.admin.worldcontrols`  | Manage world-level flags                        | ❌ op   |
| `proshield.admin.bypass`         | Toggle bypass protection                        | ❌ op   |

---

## 📖 Documentation
- 📜 [CHANGELOG.md](CHANGELOG.md) – Full version history  
- ❓ [FAQ.md](FAQ.md) – Common questions & troubleshooting  

---

## 🚀 Roadmap
ProShield **2.0** (in development 🚧):  
- 🏘️ Towns & Shops  
- 💰 Economy Integration  
- ⚙️ Per-player permission overrides  
- 🎨 Entry/Exit messages  
- 🗺️ Dynmap/BlueMap overlays  

👉 See progress: [ROADMAP-2.0.md](ROADMAP-2.0.md)  

---

💡 *ProShield is ideal for small to medium SMP servers that want strong protection, easy setup, and admin control — without heavy region plugins.*  

