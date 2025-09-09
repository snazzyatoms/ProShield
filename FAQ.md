# ❓ ProShield FAQ (v1.2.4)

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
- Slot 31 → Help (shows commands based on your permissions)  
- Slot 33 → Admin menu (if you have admin perms)  

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
This refreshes **config.yml**, protections, claim expiry, and **role settings** without restarting the server.  
🔹 New in **1.2.4**: A **Reload button** is available in the **Admin GUI**.

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

**Q: How does Mob Repel work? (NEW in 1.2.4)**  
A: Hostile mobs are **pushed back** at claim borders:  
- Configurable in `protection.mobs.border-repel`.  
- Radius, push force, and tick interval can be changed.  
- Prevents mobs from entering claims at all.  

---

**Q: Where are claims stored? Can I back them up?**  
A: Claims and trust data are stored in:  
`/plugins/ProShield/config.yml`  

✅ Always back up this file (or the full ProShield folder) before updates.

---

**Q: Do I need to delete my config for 1.2.4?**  
A: ⚠️ Yes, in some cases.  
Because of major **config restructuring** in 1.2.4, you may need to:  
1. Stop the server.  
2. Delete `/plugins/ProShield/`.  
3. Restart to regenerate fresh configs.  
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

**Problem:** Claims aren’t saving after restart  
✅ Fix:  
- Ensure the server can **write to** `plugins/ProShield/config.yml`.  
- Avoid YAML syntax errors when editing manually.  
- Run `/proshield reload` after manual edits.

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

**Problem:** Mobs still walk into claims  
✅ Fix:  
- Ensure `protection.mobs.border-repel.enabled: true`.  
- Adjust `radius` and push strength in config.  
- Reload after edits.

---

**Problem:** GUI feels tedious (need to exit to return)  
✅ Fix:  
- v1.2.3 added a **Back button** in all menus.  
- v1.2.4 fixed **Back button bugs** in Player & Admin GUIs.  

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
- For **1.2.4 only**, you may need to **delete and regenerate configs**.  
- Test updates on a dev/test server before going live.  
- Join the **Spigot discussion thread** for help & tips.
