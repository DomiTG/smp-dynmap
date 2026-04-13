package com.domitg.smpdynmap.update;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.ChatMessage;
import com.domitg.smpdynmap.model.PlayerInfo;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains the live state that the web clients poll:
 * <ul>
 *   <li>Player positions / stats</li>
 *   <li>Recent chat history</li>
 *   <li>Tile-change notifications (for cache-busting)</li>
 * </ul>
 *
 * <p>All write operations are safe to call from any thread.</p>
 */
public class UpdateManager {

    private final SmpDynmap plugin;

    /** world name → list of currently visible online players */
    private final Map<String, List<PlayerInfo>> playersByWorld = new ConcurrentHashMap<>();

    /** Players hidden from the map (UUID → true) */
    private final Set<UUID> hiddenPlayers = Collections.synchronizedSet(new HashSet<>());

    /** Global chat history (capped at config limit) */
    private final List<ChatMessage> chatHistory = new CopyOnWriteArrayList<>();
    private final int maxChatHistory;

    /** Tiles that changed since last update (world → set of "tileX:tileZ") */
    private final Map<String, Set<String>> changedTiles = new ConcurrentHashMap<>();

    private final long startTime = System.currentTimeMillis();

    public UpdateManager(SmpDynmap plugin) {
        this.plugin = plugin;
        this.maxChatHistory = plugin.getDynmapConfig().getMaxChatHistory();
    }

    // ─── Player tracking ───────────────────────────────────────────────────

    /** Refresh all player snapshots. Called from the Bukkit scheduler (main thread). */
    public void refreshPlayers() {
        boolean hideByDefault = plugin.getDynmapConfig().isHideByDefault();
        boolean hideSpectators = plugin.getDynmapConfig().isHideSpectators();

        Map<String, List<PlayerInfo>> newByWorld = new HashMap<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            boolean isHiddenBySet = hiddenPlayers.contains(player.getUniqueId());
            // If hide-by-default: only show players who have explicitly called /dynmap show
            // We track "explicitly shown" as NOT being in the hiddenPlayers set when
            // hideByDefault is active.  For simplicity we reuse the same set but invert
            // the meaning when the flag is set.
            if (hideByDefault && !isHiddenBySet) {
                // hide-by-default: skip unless the player is in the "shown" set
                // (isHiddenBySet == false means they haven't called /dynmap show yet)
                continue;
            }
            if (!hideByDefault && isHiddenBySet) continue;
            if (hideSpectators && player.getGameMode() == GameMode.SPECTATOR) continue;
            if (!player.hasPermission("smpdynmap.show") && !player.isOp()) continue;

            String worldName = player.getWorld().getName();
            if (!plugin.getDynmapConfig().isWorldEnabled(worldName)) continue;

            double health = player.getHealth();
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
            int armor = player.getInventory().getArmorContents() != null
                    ? calculateArmor(player) : 0;

            PlayerInfo info = new PlayerInfo(
                    player.getName(),
                    player.getDisplayName(),
                    worldName,
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getLocation().getYaw(),
                    player.getLocation().getPitch(),
                    health,
                    armor,
                    false
            );
            newByWorld.computeIfAbsent(worldName, k -> new ArrayList<>()).add(info);
        }

        playersByWorld.clear();
        playersByWorld.putAll(newByWorld);
    }

    private int calculateArmor(Player player) {
        // Return total armor points (each slot contributes its defence value):
        // Boots=1, Leggings=2, Chestplate=3, Helmet=1 in simplified terms.
        // We expose a 0–4 count (one per non-null slot) which the web viewer
        // can scale as needed; accurate point values require NMS access.
        org.bukkit.inventory.ItemStack[] pieces = player.getInventory().getArmorContents();
        if (pieces == null) return 0;
        int count = 0;
        for (org.bukkit.inventory.ItemStack item : pieces) {
            if (item != null) count++;
        }
        return count;
    }

    public List<PlayerInfo> getPlayersInWorld(String worldName) {
        return Collections.unmodifiableList(
                playersByWorld.getOrDefault(worldName, Collections.emptyList()));
    }

    public List<PlayerInfo> getAllPlayers() {
        List<PlayerInfo> all = new ArrayList<>();
        playersByWorld.values().forEach(all::addAll);
        return Collections.unmodifiableList(all);
    }

    // ─── Visibility ────────────────────────────────────────────────────────

    public void hidePlayer(UUID uuid) { hiddenPlayers.add(uuid); }

    public void showPlayer(UUID uuid) { hiddenPlayers.remove(uuid); }

    public boolean isHidden(UUID uuid) { return hiddenPlayers.contains(uuid); }

    // ─── Chat ──────────────────────────────────────────────────────────────

    public void addChatMessage(ChatMessage message) {
        chatHistory.add(message);
        while (chatHistory.size() > maxChatHistory) {
            chatHistory.remove(0);
        }
    }

    public List<ChatMessage> getChatSince(long timestamp) {
        List<ChatMessage> result = new ArrayList<>();
        for (ChatMessage msg : chatHistory) {
            if (msg.getTimestamp() > timestamp) result.add(msg);
        }
        return result;
    }

    public List<ChatMessage> getRecentChat(int count) {
        int size = chatHistory.size();
        int from = Math.max(0, size - count);
        return Collections.unmodifiableList(chatHistory.subList(from, size));
    }

    // ─── Tile change tracking ──────────────────────────────────────────────

    public void markTileChanged(String worldName, int tileX, int tileZ) {
        changedTiles.computeIfAbsent(worldName, k -> Collections.synchronizedSet(new HashSet<>()))
                .add(tileX + ":" + tileZ);
    }

    public Set<String> drainChangedTiles(String worldName) {
        Set<String> tiles = changedTiles.remove(worldName);
        return tiles != null ? tiles : Collections.emptySet();
    }

    // ─── Misc ──────────────────────────────────────────────────────────────

    public long getServerUptime() {
        return System.currentTimeMillis() - startTime;
    }
}
