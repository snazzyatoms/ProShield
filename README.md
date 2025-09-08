# 🛡️ ProShield

ProShield is a lightweight land and plot protection plugin for Spigot & Paper servers.  
Perfect for survival or SMP communities, it provides simple claiming, intuitive GUIs, and powerful admin tools without the bloat.

---

## ✨ Features

- ✅ **Land Claiming** – Players can protect chunks with one command or the ProShield menu.  
- ✅ **Unclaiming** – Release land instantly when no longer needed.  
- ✅ **Protection System** – Prevents griefing (block break/place, containers, interactions, fire spread, TNT/creeper explosions, entity grief).  
- ✅ **Trust System** – Owners can `/proshield trust <player>` to allow friends access.  
- ✅ **Claim Roles (since 1.2.1)** – Define granular access levels: Visitor, Member, Container, Builder, Co-Owner.  
- ✅ **Keep Items in Claims (NEW in 1.2.2)** – Optional toggle to prevent items dropped in claims from despawning (configurable, off by default).  
- ✅ **GUI Menu** – Clean and simple inventory menu for claiming, info, unclaiming, and help.  
- ✅ **Admin Compass** – Special compass item that opens the ProShield GUI. Auto-given to ops (configurable).  
- ✅ **Claim Expiry** – Automatically removes claims of inactive players (optional, fully configurable).  
- ✅ **Configurable Protections** – Containers, doors, buttons, buckets, fire, mobs, explosions, Enderman teleport, and more.  
- ✅ **Lightweight** – Built for performance and ease of use.  

---

## 🏡 Getting Started: How to Claim Land

Players can claim land in two ways:

### 🔹 Option 1: The ProShield Compass (GUI)
- Operators/admins automatically receive it on join.  
- Players with permissions can get one via `/proshield compass`.  
- Right-click to open the menu:  
  - Slot 11 → Claim current chunk  
  - Slot 13 → View claim info  
  - Slot 15 → Unclaim chunk  
  - Slot 31 → Help (shows commands relevant to your role/permissions)  
  - Slot 33 → Admin menu (for players with admin perms)  

### 🔹 Option 2: Commands
- `/proshield claim` – Claim your current chunk  
- `/proshield unclaim` – Remove your claim  
- `/proshield info` – View claim info (owner, trusted players)  
- `/proshield trust <player> [role]` – Grant access with a role (Visitor/Member/Builder/etc.)  
- `/proshield untrust <player>` – Remove access  
- `/proshield trusted` – List trusted players  

⚡ Tip: Use the **Compass GUI** for ease, or commands for precision.

---

## ⌨️ Commands

- `/proshield` – Main command + help  
- `/proshield claim` – Claim your current chunk  
- `/proshield unclaim` – Remove your claim  
- `/proshield info` – Show claim info (owner, trusted players)  
- `/proshield trust <player> [role]` – Grant access to a player with optional role  
- `/proshield untrust <player>` – Remove trust  
- `/proshield trusted` – List trusted players  
- `/proshield compass` – Give yourself the ProShield compass  
- `/proshield bypass <on|off|toggle>` – Toggle admin bypass  
- `/proshield reload` – Reload configuration  
- `/proshield purgeexpired <days> [dryrun]` – Force claim expiry cleanup (admins)  

---

## 🔑 Permissions

| Node                             | Description                                                | Default |
|----------------------------------|------------------------------------------------------------|---------|
| `proshield.use`                  | Use ProShield commands and GUI                             | ✅ true |
| `proshield.admin`                | Access admin tools (compass, bypass, force unclaim, purge) | ❌ op   |
| `proshield.compass`              | Receive/use ProShield compass                              | ❌ op   |
| `proshield.bypass`               | Toggle bypass protection                                   | ❌ op   |
| `proshield.unlimited`            | Ignore max-claims limit                                    | ❌ op   |
| `proshield.admin.tp`             | Teleport to claims from admin menu                         | ❌ op   |
| `proshield.admin.reload`         | Use `/proshield reload`                                    | ❌ op   |
| `proshield.admin.expired.purge`  | Manage expired claims                                      | ❌ op   |
| `proshield.admin.keepdrops`      | Toggle item-keep inside claims (1.2.2+)                    | ❌ op   |

---

## 📖 Documentation

- [Changelog](CHANGELOG.md) – Full history of changes  
- [FAQ](FAQ.md) – Common questions & troubleshooting  

---

## 🚀 Roadmap

ProShield **2.0** is in the works!  
It will introduce **Towns, Shops, Economy Integration, Claim Flags, Entry/Exit Messages, Map Overlays, and more**.  

👉 [See the 2.0 Roadmap](ROADMAP-2.0.md)  

---

💡 ProShield is ideal for small to medium survival servers that want protection, simplicity, and admin control without requiring heavy region plugins.
