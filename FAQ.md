# ❓ ProShield FAQ (v1.2.5)

This FAQ covers the most common questions, problems, and solutions for running ProShield.  
If your issue isn’t listed here, please [open a GitHub issue](https://github.com/snazzyatoms/ProShield/issues).

---

## ✅ General

**Q: What Minecraft versions are supported?**  
A: ProShield supports **1.18 → 1.21** on **Spigot and Paper**.  
Newer versions will be tested as they release.

---

**Q: Do I need a permissions plugin?**  
A: **No.** ProShield works with Bukkit’s native permissions out-of-the-box.  
If you run **LuckPerms, PermissionsEx, or GroupManager**, ProShield integrates seamlessly.

---

**Q: How do players claim land?**  
A: Players can claim in **two ways**:

🔹 **Compass GUI**  
- Use the ProShield Compass (right-click).  
- Slot 11 → Claim current chunk  
- Slot 13 → Claim info (owner + trusted players)  
- Slot 15 → Unclaim  
- Slot 28 → Claim Flags menu  
- Slot 30 → Expansion Requests (NEW in v1.2.5)  
- Slot 31 → Help (shows commands based on your permissions)  
- Slot 32 → Admin Tools (if you have admin perms)  

🔹 **Commands**  
- `/proshield claim` → Claim your current chunk  
- `/proshield unclaim` → Unclaim  
- `/proshield info` → View claim details  

---

**Q: I didn’t get the compass when joining. What’s wrong?**  
A: Check these points:  
- You are **OP** (ops automatically get the **Admin Compass**) or have `proshield.compass` / `proshield.admin`.  
- In `config.yml`, `autogive.compass-on-join: true`.  
- Use `/proshield compass` to get one manually if missing.  

---

**Q: Can I reload configs without restarting?**  
A: Yes! Run `/proshield reload`.  
This refreshes **config.yml**, **messages.yml**, protections, claims, expiry, and expansions.  
🔹 Since **1.2.4**, a **Reload button** is available in the **Admin GUI**.  
🔹 Expanded in **1.2.5** to also reload expansions.yml.  

---

**Q: How does claim expiry work?**  
A: Expiry **automatically removes claims** from inactive players:  
- Enable with `expiry.enabled: true`.  
- `expiry.days` sets the inactivity limit.  
- Runs on **startup** and **daily**.  
- Admins can force it with `/proshield purgeexpired`.

---

**Q: What are Claim Roles?**  
A: Claim Roles let owners assign **different access levels** to trusted players:  
- **Visitor** → Walk only, no interaction.  
- **Member** → Basic interaction (doors, buttons, levers).  
- **Container** → Can use chests, barrels, furnaces.  
- **Builder** → Build and break blocks.  
- **Co-Owner** → Full access, almost like the owner.  

Configured in `config.yml` and manageable via **commands & GUI**.  
More role customization coming in **v2.0**.

---

**Q: What is Claim Transfer?**  
A: Owners can transfer their claim to another player with:  
- `/proshield transfer <player>`  

---

**Q: What is Claim Preview?**  
A: Use `/proshield preview` to see a **particle outline** of your claim before confirming.  

---

**Q: What is Spawn Guard? (NEW in 1.2.4)**  
A: Spawn Guard prevents claims too close to the **world spawn**:  
- Configurable radius via `spawn.radius`.  
- Default = 32 blocks.  
- Admins with `proshield.admin.bypass` can override it.  

---

**Q: How does Mob Repel work? (IMPROVED in 1.2.5)**  
A: Hostile mobs are **pushed back** at claim or safezone borders:  
- Configurable in `protection.mobs.border-repel`.  
- Radius, push force, and tick interval can be changed.  
- Since **1.2.5**, mobs inside safezones are also despawned automatically.  
- Pathfinding/targeting into safezones is blocked.  

---

**Q: What are Expansion Requests? (NEW in 1.2.5)**  
A: Players can request more claim blocks through a GUI:  
- Increase/decrease expansion size (+16/+32/+64, -16/-32/-64).  
- Submit to admins for review.  
- Configurable defaults in `config.yml`.  

---

**Q: How do admins handle Expansion Requests? (NEW in 1.2.5)**  
A: Through the **Admin GUI**:  
- **Expansion Requests menu** → approve or deny.  
- Denial reasons are pulled from `messages.yml`.  
- History is tracked with pagination for audit.  

---

**Q: What are World Controls? (NEW in 1.2.5)**  
A: A dedicated **Admin-only GUI** to toggle global protections:  
- Fire spread  
- Explosions (TNT/creeper)  
- Mob damage  
- Other environmental flags  

All synced with `config.yml` and applied globally.

---

**Q: Where are claims stored? Can I back them up?**  
A: Claims and trust data are stored in:  
`/plugins/ProShield/config.yml`  

✅ Always back up this file (or the full ProShield folder) before updates.

---

**Q: Do I need to delete my config for 1.2.5?**  
A: ⚠️ Yes.  
Because of major **config and messages.yml restructuring** in 1.2.5, you may need to:  
1. Stop the server.  
2. Delete `/plugins/ProShield/`.  
3. Restart to regenerate fresh configs and messages.  
4. Reapply your custom edits.

---

**Q: Where do I report bugs or request features?**  
A: Use the [GitHub Issues page](https://github.com/snazzyatoms/ProShield/issues) or the **Spigot discussion thread**.

---

## 🛠️ Troubleshooting

**Problem:** “No permission” even though I’m OP  
✅ Fix:  
- Ensure correct permission nodes (`proshield.use`, `proshield.admin`, etc.).  
- Double-check LuckPerms or your permissions plugin.  

---

**Problem:** Players can’t build/interact in claims (but should)  
✅ Fix:  
- Confirm they are **trusted** with `/proshield trust <player>`.  
- Check their **role** (Visitor/Member/Container/Builder/Co-Owner).  
- Review `protection.interactions` in `config.yml`.

---

**Problem:** Players can interact when they **shouldn’t**  
✅ Fix:  
- Ensure `protection.interactions.enabled: true`.  
- Verify categories (doors, buttons, etc.).  
- Run `/proshield reload` after edits.

---

**Problem:** Expansion Requests aren’t working  
✅ Fix:  
- Ensure `claims.expansion.enabled: true` in `config.yml`.  
- Check that admins have `proshield.admin.expansions`.  
- Review `messages.yml` for missing deny reasons.  

---

**Problem:** Safezones not protecting from mobs  
✅ Fix:  
- Ensure `protection.mobs.*` settings are enabled.  
- Reload after edits.  
- Check that claims marked with `safezone: true` are active.  

---

**Problem:** Claims aren’t saving after restart  
✅ Fix:  
- Ensure the server can **write to** `plugins/ProShield/config.yml`.  
- Avoid YAML syntax errors when editing manually.  
- Run `/proshield reload` after manual edits.

---

**Problem:** GUI buttons don’t do anything  
✅ Fix:  
- Make sure you are running **v1.2.5** or newer (GUI fixes applied).  
- Check for permission nodes like `proshield.player.access` or `proshield.admin`.  

---

**Problem:** Claim expiry isn’t working  
✅ Fix:  
- Check `expiry.enabled: true`.  
- Verify `expiry.days` is reasonable.  
- Wait for the **daily task** or use `/proshield purgeexpired`.

---

**Problem:** Fire, TNT, creepers, or mobs still grief claims  
✅ Fix:  
- Review `protection.*` flags in `config.yml`.  
- Confirm **per-world overrides** aren’t disabling protection.  
- Reload after changes.

---

**Problem:** GUI feels tedious (need to exit to return)  
✅ Fix:  
- v1.2.3 added a **Back button** in all menus.  
- v1.2.4 fixed **Back button bugs**.  
- v1.2.5 made Back/Exit consistent and functional across *all* GUIs.  

---

**Problem:** Need more details for debugging  
✅ Fix:  
- Use `/proshield debug on` to enable detailed logging.  
- Requires `proshield.admin.debug`.  

---

**Problem:** JAR won’t load / “Invalid plugin.yml”  
✅ Fix:  
- Ensure `plugin.yml` is inside `src/main/resources/`.  
- Run `mvn clean install` for a fresh build.  
- Don’t rename the JAR manually.

---

## 📌 Notes

- Always back up `/plugins/ProShield/` before updating.  
- For **1.2.5**, you must **delete and regenerate configs + messages.yml**.  
- Test updates on a dev/test server before going live.  
- Join the **Spigot discussion thread** for help & tips.  
