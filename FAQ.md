# ❓ ProShield FAQ

This FAQ covers the most common questions, problems, and solutions for running ProShield.  
If your issue isn’t listed here, please open a GitHub issue or post on the Spigot thread.

---

## ✅ General

### Q: What Minecraft versions are supported?
A: ProShield supports **1.18 → 1.21** on Spigot and Paper.  
Newer versions will be tested as they release.

---

### Q: Do I need a permissions plugin?
A: No. ProShield works out-of-the-box with Bukkit’s native permissions.  
But if you use **LuckPerms, PermissionsEx, or GroupManager**, ProShield integrates seamlessly.

---

### Q: How do players claim land?
A: Two methods are available:

1. **Compass GUI**  
   - Use the ProShield Compass (right-click).  
   - Slot 11 → Claim chunk  
   - Slot 13 → Info (owner + trusted)  
   - Slot 15 → Unclaim  

2. **Commands**  
   - `/proshield claim` → Claim your current chunk  
   - `/proshield unclaim` → Unclaim  
   - `/proshield info` → View details  

---

### Q: I didn’t get the compass when joining. What’s wrong?
A: Check these points:
- You are OP or have `proshield.compass` or `proshield.admin`.  
- In `config.yml`, `autogive.compass-on-join: true`.  
- If missing, use `/proshield compass` to get one manually.

---

### Q: Can I reload configs without restarting?
A: Yes! Run `/proshield reload`.  
This refreshes **config.yml**, protections, claim expiry, and role settings without restarting.

---

### Q: How does claim expiry work?
A: Expiry automatically removes claims from inactive players.  
- Enabled via `expiry.enabled: true` in `config.yml`.  
- `expiry.days` defines inactivity length.  
- Expiry runs on startup and once per day.  
- Admins can force it with `/proshield purgeexpired`.

---

### Q: What are Claim Roles?
A: Claim Roles give trusted players different **access levels**.  
Examples:  
- **Builder** → place/break blocks  
- **Container** → open chests, barrels  
- **Visitor** → walk only  

Configured in `config.yml`. More GUI features coming in **v2.0**.

---

### Q: Where are claims stored? Can I back them up?
A: Claims and trust lists are saved in `plugins/ProShield/config.yml`.  
Always back this up (or the entire folder) before updating.

---

### Q: Where do I report bugs or request features?
A: Please use the **GitHub Issues page** or the **Spigot discussion thread**.

---

## 🛠️ Troubleshooting

### Problem: “No permission” even though I’m OP
✅ Fix:  
- Verify correct permission nodes (`proshield.use`, `proshield.admin`, etc).  
- Double-check server configs or permission plugins (e.g., LuckPerms).

---

### Problem: Players can’t build/interact in claims (but should)
✅ Fix:  
- Confirm they are **trusted** with `/proshield trust <player>`.  
- Check `protection.interactions` in `config.yml`.  
- Verify their **role** (`Builder`, `Container`, etc).

---

### Problem: Players can still interact when they shouldn’t
✅ Fix:  
- Ensure `protection.interactions.enabled: true`.  
- Review categories (`doors`, `buttons`, etc).  
- Reload with `/proshield reload`.

---

### Problem: Claims aren’t saving after restart
✅ Fix:  
- Ensure server has write permission to `plugins/ProShield/config.yml`.  
- Avoid YAML syntax errors when editing manually.  
- Run `/proshield reload` after manual changes.

---

### Problem: Claim expiry isn’t working
✅ Fix:  
- Check `expiry.enabled: true`.  
- Verify `expiry.days` is reasonable.  
- Wait for daily task or use `/proshield purgeexpired`.

---

### Problem: Fire, TNT, creepers, or mobs still cause griefing
✅ Fix:  
- Check `protection.*` flags in `config.yml`.  
- Confirm per-world overrides aren’t disabling protection.  
- Reload after edits.

---

### Problem: JAR won’t load / “Invalid plugin.yml”
✅ Fix:  
- Ensure `plugin.yml` exists in `src/main/resources/`.  
- Run `mvn clean install` for a fresh build.  
- Don’t rename the JAR manually.

---

## 📌 Notes

- Always back up `plugins/ProShield/` before updating.  
- Test updates on a **dev server** before production.  
- Join the **Spigot discussion thread** for help and community tips.
