# ❓ ProShield FAQ (v1.2.6)

[![SpigotMC](https://img.shields.io/badge/Spigot-Paper-blue)]() 
[![Minecraft](https://img.shields.io/badge/MC-1.18--1.21-green)]() 
[![Multilingual](https://img.shields.io/badge/🌐-Multilingual-lightblue)]() 
[![GUI-First](https://img.shields.io/badge/🖥️-GUI--First-orange)]() 
[![Lightweight](https://img.shields.io/badge/⚡-Lightweight-yellow)]() 

> ProShield is designed to be **lightweight, GUI-first, multilingual, and admin-friendly**.  
> This FAQ covers the most common questions, problems, and solutions.  
If your issue isn’t listed, please [open a GitHub issue](https://github.com/snazzyatoms/ProShield/issues).

---

## ✅ General

**Q: What Minecraft versions are supported?**  
A: ProShield supports **1.18 → 1.21+** on **Spigot and Paper**.  
Newer versions will be tested as they release.

---

**Q: Do I need a permissions plugin?**  
A: **No.** ProShield works with Bukkit’s native permissions out-of-the-box.  
If you run **LuckPerms, PermissionsEx, or GroupManager**, ProShield integrates seamlessly.

---

**Q: How do players claim land?**  
A: Players can claim in **two ways**:

🔹 **Compass GUI (recommended)**  
- Use the ProShield Compass (right-click).  
- Slot 11 → Claim current chunk  
- Slot 13 → Claim Info (owner + trusted players)  
- Slot 15 → Unclaim  
- Slot 16 → Trusted Players (roles)  
- Slot 28 → Claim Flags (PvP, Safezone, Crop Trample, etc.)  
- Slot 30 → Expansion Requests  
- Slot 32 → Admin Tools (if you have admin perms)  

🔹 **Commands (fallback)**  
- `/proshield claim` → Claim your current chunk  
- `/proshield unclaim` → Unclaim  
- `/proshield info` → View claim details  
- `/proshield trust <player> [role]` → Grant access  
- `/proshield untrust <player>` → Remove trust  
- `/proshield trusted` → List trusted players  

💡 *All features are GUI-first. Commands are optional for precision only.*

---

**Q: I didn’t get the compass when joining. What’s wrong?**  
A: Check these points:  
- You are OP or have `proshield.compass` / `proshield.admin`.  
- In `config.yml`, `settings.give-compass-on-join: true`.  
- Use `/proshield compass` to get one manually if missing.  
- ✅ Since **1.2.6**, compass handling was unified → no duplicates, reliable detection.

---

**Q: Can I reload configs without restarting?**  
A: Yes! Run `/proshield reload`.  
This refreshes **config.yml**, **messages.yml**, protections, claims, expiry, and expansions.  
🔹 Since **1.2.4**, a **Reload button** was added in the Admin GUI.  
🔹 In **1.2.6**, the GUI reload button was fixed and is fully functional again.

---

**Q: How does claim expiry work?**  
A: Expiry **automatically removes claims** from inactive players:  
- Enable with `expiry.enabled: true`.  
- `expiry.days` sets the inactivity limit.  
- Runs on **startup** and **daily**.  
- Admins can force it with `/proshield purgeexpired`.

---

**Q: What are Claim Roles?**  
A: Roles define what trusted players can do:  
- **Visitor** → Walk only, no interaction.  
- **Member** → Containers only.  
- **Builder** → Can build/break + containers.  
- **Trusted** → Full access except ownership.  
- **Moderator/Manager** → Manage roles & flags.  
- **Owner** → Full control.  

Configured in `config.yml` and manageable via the **Trusted Players GUI**.  
🎯 Finer-grained per-player controls are planned for **v2.0**.

---

**Q: What is Claim Transfer?**  
A: Owners can transfer their claim to another player with:  
- `/proshield transfer <player>`  

---

**Q: What is Claim Preview?**  
A: Use `/proshield preview` to see a **particle outline** of your claim before confirming.  

---

**Q: What is Spawn Guard? (1.2.4)**  
A: Spawn Guard prevents claims too close to the **world spawn**:  
- Configurable radius via `spawn.radius`.  
- Admins with `proshield.admin.bypass` can override it.  

---

**Q: How does Mob Repel work? (improved 1.2.5)**  
A: Hostile mobs are **pushed back** at claim or safezone borders:  
- Configurable in `protection.mobs.border-repel`.  
- Mobs inside safezones are automatically despawned.  
- Pathfinding/targeting into safezones is blocked.

---

**Q: What is the Crop Trample flag? (NEW in 1.2.6)**  
A: A claim flag that toggles whether players/animals can trample farmland.  
- Default: **true** (prevent trample).  
- Toggleable in the **Flags GUI** or via config.

---

**Q: What are Expansion Requests?**  
A: Players can request more claim blocks via GUI:  
- Options are configurable (e.g., +5, +10, +20).  
- Requests queue for admin approval/denial.  
- Admins can deny with custom reasons (`messages.yml`).  
- History is saved for transparency.

---

**Q: What are World Controls?**  
A: An **Admin-only GUI** for global toggles like:  
- Fire spread  
- Explosions  
- Mob spawn/damage  
- Crop trample  
- Container access  

🌍 In **1.2.6**, placeholders were added for Nether/End worlds (reserved for v2.0+).

---

**Q: Is ProShield multilingual? (NEW in 1.2.6)**  
A: ✅ Yes. All texts (errors, GUI labels, lore, deny reasons) are stored in `messages.yml`.  
- Server owners can freely **translate, customize, or rewrite** any line.  
- No dev updates needed — your ProShield can speak **any language you want**.  
- 🌐 You can polish translations, rewrite lore lines, or even re-theme messages to match your server’s personality.  

---

**Q: Where are claims stored?**  
A: Claims and trust data are stored in:  
`/plugins/ProShield/config.yml`  

✅ Always back this file up before updates.

---

**Q: Do I need to delete configs for 1.2.6?**  
A: ⚠️ Yes, if coming from older versions.  
Because of new features (Crop Trample, GUI polish, multilingual sync), you must:  
1. Stop the server.  
2. Backup & delete `/plugins/ProShield/`.  
3. Restart → new configs/messages generated.  
4. Reapply your custom edits (especially translations).  

---

## 🛠️ Troubleshooting

**Problem:** GUI buttons don’t work (Admin Tools, Expansion Requests, World Controls)  
✅ Fix: Update to **1.2.6**. These menus were fixed in this release.  

---

**Problem:** Compass doesn’t open the menu  
✅ Fix:  
- Use the official ProShield Compass (right-click).  
- Since **1.2.6**, detection was unified — custom/renamed compasses won’t work.  
- If missing, run `/proshield compass` or check config.  

---

**Problem:** Safezones not protecting from mobs  
✅ Fix:  
- Ensure `claims.safezone-enabled: true`.  
- Check `protection.mobs.*` settings.  
- Reload after edits.  

---

**Problem:** Crop trample still happens  
✅ Fix:  
- Ensure `flags.crop-trample` exists in `config.yml`.  
- Default is **true** (protected).  
- Toggle via GUI → Claim Flags → Crop Trample.  

---

**Problem:** Expansion Requests don’t show  
✅ Fix:  
- Ensure `claims.expansion.enabled: true`.  
- Check permissions: `proshield.admin.expansions`.  
- Review `messages.yml` for deny reasons.  

---

**Problem:** Claims not saving  
✅ Fix:  
- Ensure the server can write to `config.yml`.  
- Check YAML formatting.  
- Use `/proshield reload` after manual edits.  

---

**Problem:** Debug logging is too noisy  
✅ Fix:  
- In `config.yml`, set `settings.debug.*` sections to **false**.  
- Debug can be toggled by admins with `/proshield debug`.  

---

## 📌 Notes

- Always back up `/plugins/ProShield/` before updating.  
- **1.2.6 requires regeneration** of configs/messages for new features.  
- ProShield is **GUI-first** — commands exist as fallbacks only.  
- ProShield is fully **multilingual** via `messages.yml`.  

---
