package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.ChatMessage;
import com.domitg.smpdynmap.model.PlayerInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.World;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Serves the periodic update JSON polled by web clients.
 *
 * <pre>GET /up/world/{worldName}/{sinceTimestamp}</pre>
 *
 * Returns player positions, recent chat, tile change notifications, and
 * world state (time, weather).
 */
public class UpdateHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final boolean allowCors;

    public UpdateHandler(SmpDynmap plugin) {
        this.plugin = plugin;
        this.allowCors = plugin.getDynmapConfig().isAllowCors();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (HttpUtil.handleOptions(ex)) return;
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendError(ex, 405, "Method Not Allowed");
            return;
        }

        // Path: /up/world/{worldName}/{timestamp}
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        // parts[0]="" [1]="up" [2]="world" [3]=worldName [4]=timestamp(optional)

        if (parts.length < 4) {
            HttpUtil.sendError(ex, 400, "Bad Request: missing world name");
            return;
        }

        String worldName = parts[3];
        long sinceTimestamp = 0;
        if (parts.length >= 5) {
            try {
                sinceTimestamp = Long.parseLong(parts[4]);
            } catch (NumberFormatException ignored) { /* use 0 */ }
        }

        World world = plugin.getServer().getWorld(worldName);
        JsonObject root = new JsonObject();
        long now = System.currentTimeMillis();
        root.addProperty("timestamp", now);
        root.addProperty("servertime", world != null ? world.getTime() : 0);
        root.addProperty("hasStorm", world != null && world.hasStorm());
        root.addProperty("isThundering", world != null && world.isThundering());
        root.addProperty("timeofday", world != null ? world.getTime() : 0);

        // Players
        JsonArray players = new JsonArray();
        List<PlayerInfo> playerList = plugin.getUpdateManager().getPlayersInWorld(worldName);
        for (PlayerInfo p : playerList) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("account", p.getName());
            pObj.addProperty("name", p.getName());
            pObj.addProperty("displayName", p.getDisplayName());
            pObj.addProperty("world", p.getWorld());
            pObj.addProperty("x", p.getX());
            pObj.addProperty("y", p.getY());
            pObj.addProperty("z", p.getZ());
            pObj.addProperty("yaw", p.getYaw());
            pObj.addProperty("pitch", p.getPitch());
            pObj.addProperty("health", p.getHealth());
            pObj.addProperty("armor", p.getArmor());
            pObj.addProperty("sort", 0);
            pObj.addProperty("type", "player");
            players.add(pObj);
        }
        root.add("players", players);
        root.addProperty("currentcount", players.size());

        // Updates (chat + tile changes)
        JsonArray updates = new JsonArray();

        // Chat messages since the given timestamp
        List<ChatMessage> chats = plugin.getUpdateManager().getChatSince(sinceTimestamp);
        for (ChatMessage msg : chats) {
            JsonObject chatObj = new JsonObject();
            chatObj.addProperty("type", "chat");
            chatObj.addProperty("playerName", msg.getPlayerName());
            chatObj.addProperty("message", msg.getMessage());
            chatObj.addProperty("channel", msg.getChannel());
            chatObj.addProperty("timestamp", msg.getTimestamp());
            updates.add(chatObj);
        }

        // Tile change notifications
        Set<String> changedTiles = plugin.getUpdateManager().drainChangedTiles(worldName);
        for (String tile : changedTiles) {
            String[] tParts = tile.split(":");
            if (tParts.length != 2) continue;
            JsonObject tileObj = new JsonObject();
            tileObj.addProperty("type", "tilechange");
            tileObj.addProperty("name", tParts[0] + "_" + tParts[1]);
            tileObj.addProperty("x", Integer.parseInt(tParts[0]));
            tileObj.addProperty("z", Integer.parseInt(tParts[1]));
            updates.add(tileObj);
        }

        root.add("updates", updates);

        HttpUtil.sendJson(ex, 200, root.toString(), allowCors);
    }
}
