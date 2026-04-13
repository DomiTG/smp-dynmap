package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.DynmapConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.World;

import java.io.IOException;
import java.util.List;

/**
 * Serves the server configuration JSON consumed by web clients.
 *
 * <pre>GET /up/configuration</pre>
 */
public class ConfigurationHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final boolean allowCors;

    public ConfigurationHandler(SmpDynmap plugin) {
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

        DynmapConfig cfg = plugin.getDynmapConfig();
        JsonObject root = new JsonObject();

        root.addProperty("dynmapversion", plugin.getDescription().getVersion());
        root.addProperty("coreversion", plugin.getDescription().getVersion());
        root.addProperty("servertime", plugin.getServer().getWorlds().isEmpty() ? 0
                : plugin.getServer().getWorlds().get(0).getTime());
        root.addProperty("playercount", plugin.getServer().getOnlinePlayers().size());
        root.addProperty("defaultworld", cfg.getDefaultWorld());
        root.addProperty("defaultmap", cfg.getDefaultMap());
        root.addProperty("defaultzoom", cfg.getDefaultZoom());
        root.addProperty("mapzoomin", 2);
        root.addProperty("mapzoomout", cfg.getZoomLevels());
        root.addProperty("showplayerfacesinmenu", cfg.isShowPlayerFaces());
        root.addProperty("showplayerhealth", cfg.isShowPlayerHealth());
        root.addProperty("showplayerarmor", cfg.isShowPlayerArmor());
        root.addProperty("joinmessage", "joined the game");
        root.addProperty("quitmessage", "left the game");
        root.addProperty("sidebaropened", false);
        root.addProperty("allowwebchat", cfg.isChatEnabled());
        root.addProperty("webchatcooldown", 0);
        root.addProperty("tileSize", 128);

        // Build worlds array
        JsonArray worlds = new JsonArray();
        for (String worldName : cfg.getEnabledWorlds()) {
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;

            JsonObject worldObj = new JsonObject();
            worldObj.addProperty("name", worldName);
            worldObj.addProperty("title", cfg.getWorldTitle(worldName));
            worldObj.addProperty("type", getWorldTypeString(world.getEnvironment()));
            worldObj.addProperty("protected", false);
            worldObj.addProperty("sealevel", world.getSeaLevel());
            worldObj.addProperty("center", "0,64,0");

            // Maps for this world
            JsonArray maps = new JsonArray();
            for (String mapType : cfg.getMapTypes(worldName)) {
                JsonObject mapObj = new JsonObject();
                mapObj.addProperty("name", mapType);
                mapObj.addProperty("title", cfg.getMapTitle(worldName, mapType));
                mapObj.addProperty("type", mapType);
                mapObj.addProperty("icon", "");
                mapObj.addProperty("mapzoomin", 2);
                mapObj.addProperty("mapzoomout", cfg.getZoomLevels());
                mapObj.addProperty("background", cfg.getMapBackground(worldName, mapType));
                mapObj.addProperty("nightandday", false);
                // Projection info for the web viewer
                JsonArray worldToMap = new JsonArray();
                worldToMap.add(1); worldToMap.add(0);
                worldToMap.add(0); worldToMap.add(0);
                worldToMap.add(0); worldToMap.add(1);
                worldToMap.add(0); worldToMap.add(0);
                worldToMap.add(0); worldToMap.add(0);
                worldToMap.add(1); worldToMap.add(0);
                mapObj.add("worldtomap", worldToMap);
                maps.add(mapObj);
            }
            worldObj.add("maps", maps);
            worlds.add(worldObj);
        }
        root.add("worlds", worlds);

        HttpUtil.sendJson(ex, 200, root.toString(), allowCors);
    }

    private String getWorldTypeString(World.Environment env) {
        return switch (env) {
            case NETHER -> "nether";
            case THE_END -> "the_end";
            default -> "normal";
        };
    }
}
