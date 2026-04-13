package com.domitg.smpdynmap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Typed wrapper around the Bukkit {@link FileConfiguration} for SmpDynmap.
 *
 * <p>All getters read directly from the underlying config so that a
 * {@link SmpDynmap#reloadDynmapConfig()} call takes effect immediately.</p>
 */
public class DynmapConfig {

    private final SmpDynmap plugin;

    public DynmapConfig(SmpDynmap plugin) {
        this.plugin = plugin;
    }

    // ─── HTTP ──────────────────────────────────────────────────────────────

    public boolean isHttpEnabled() {
        return cfg().getBoolean("http.enabled", true);
    }

    public int getHttpPort() {
        return cfg().getInt("http.port", 8123);
    }

    public String getHttpAddress() {
        return cfg().getString("http.address", "0.0.0.0");
    }

    public int getMaxConnections() {
        return cfg().getInt("http.maxconnections", 25);
    }

    public String getWebRoot() {
        return cfg().getString("http.webroot", "web");
    }

    public boolean isAllowCors() {
        return cfg().getBoolean("http.allowcors", true);
    }

    // ─── Worlds ────────────────────────────────────────────────────────────

    public List<String> getEnabledWorlds() {
        List<?> worlds = cfg().getList("worlds");
        if (worlds == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Object obj : worlds) {
            if (obj instanceof Map<?, ?> map) {
                Object name = map.get("name");
                Object enabled = map.get("enabled");
                if (name != null && !Boolean.FALSE.equals(enabled)) {
                    result.add(name.toString());
                }
            }
        }
        return result;
    }

    public boolean isWorldEnabled(String worldName) {
        return getEnabledWorlds().contains(worldName);
    }

    public String getWorldTitle(String worldName) {
        return getWorldString(worldName, "title", worldName);
    }

    public List<String> getMapTypes(String worldName) {
        List<?> worlds = cfg().getList("worlds");
        if (worlds == null) return Collections.singletonList("flat");
        for (Object obj : worlds) {
            if (obj instanceof Map<?, ?> worldMap) {
                if (worldName.equals(worldMap.get("name"))) {
                    Object mapsObj = worldMap.get("maps");
                    if (mapsObj instanceof List<?> maps) {
                        List<String> types = new ArrayList<>();
                        for (Object mapObj : maps) {
                            if (mapObj instanceof Map<?, ?> mapMap) {
                                Object typeVal = mapMap.get("name");
                                if (typeVal != null) types.add(typeVal.toString());
                            }
                        }
                        return types.isEmpty() ? Collections.singletonList("flat") : types;
                    }
                }
            }
        }
        return Collections.singletonList("flat");
    }

    public String getMapTitle(String worldName, String mapType) {
        return getMapString(worldName, mapType, "title", mapType);
    }

    public String getMapBackground(String worldName, String mapType) {
        return getMapString(worldName, mapType, "background", "#000000");
    }

    public String getDefaultWorld() {
        List<String> worlds = getEnabledWorlds();
        return worlds.isEmpty() ? "world" : worlds.get(0);
    }

    public String getDefaultMap() {
        return "flat";
    }

    public int getDefaultZoom() {
        return 0;
    }

    // ─── Rendering ─────────────────────────────────────────────────────────

    public int getRenderThreads() {
        return cfg().getInt("rendering.thread-count", 2);
    }

    public int getZoomLevels() {
        return cfg().getInt("rendering.zoom-levels", 4);
    }

    public int getChunkLoadTimeout() {
        return cfg().getInt("rendering.chunk-load-timeout", 5);
    }

    public int getMaxTilesInQueue() {
        return cfg().getInt("rendering.max-tiles-in-queue", 500);
    }

    public boolean isSlopeShading() {
        return cfg().getBoolean("rendering.slope-shading", true);
    }

    // ─── Updates ───────────────────────────────────────────────────────────

    public int getUpdatePeriodTicks() {
        return cfg().getInt("updates.period", 2) * 20;
    }

    // ─── Chat ──────────────────────────────────────────────────────────────

    public boolean isChatEnabled() {
        return cfg().getBoolean("chat.enable", true);
    }

    public int getMaxChatHistory() {
        return cfg().getInt("chat.maxhistory", 100);
    }

    // ─── Players ───────────────────────────────────────────────────────────

    public boolean isHideByDefault() {
        return cfg().getBoolean("players.hidebydefault", false);
    }

    public boolean isShowPlayerFaces() {
        return cfg().getBoolean("players.showplayerfaces", true);
    }

    public boolean isShowPlayerHealth() {
        return cfg().getBoolean("players.showplayerhealth", true);
    }

    public boolean isShowPlayerArmor() {
        return cfg().getBoolean("players.showplayerarmor", true);
    }

    public boolean isHideSpectators() {
        return cfg().getBoolean("players.hideinspectator", true);
    }

    // ─── Markers ───────────────────────────────────────────────────────────

    public String getMarkersStorageFile() {
        return cfg().getString("markers.storagefile", "markers.json");
    }

    // ─── Logging ───────────────────────────────────────────────────────────

    public boolean isVerboseLogging() {
        return cfg().getBoolean("logging.verboselogging", false);
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    @SuppressWarnings("unchecked")
    private String getWorldString(String worldName, String key, String def) {
        List<?> worlds = cfg().getList("worlds");
        if (worlds == null) return def;
        for (Object obj : worlds) {
            if (obj instanceof Map<?, ?> map && worldName.equals(map.get("name"))) {
                Object val = map.get(key);
                return val != null ? val.toString() : def;
            }
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    private String getMapString(String worldName, String mapType, String key, String def) {
        List<?> worlds = cfg().getList("worlds");
        if (worlds == null) return def;
        for (Object obj : worlds) {
            if (obj instanceof Map<?, ?> worldMap && worldName.equals(worldMap.get("name"))) {
                Object mapsObj = worldMap.get("maps");
                if (mapsObj instanceof List<?> maps) {
                    for (Object mapObj : maps) {
                        if (mapObj instanceof Map<?, ?> mapMap
                                && mapType.equals(mapMap.get("name"))) {
                            Object val = mapMap.get(key);
                            return val != null ? val.toString() : def;
                        }
                    }
                }
            }
        }
        return def;
    }
}
