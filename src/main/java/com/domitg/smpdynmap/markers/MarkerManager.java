package com.domitg.smpdynmap.markers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.DynmapMarker;
import com.domitg.smpdynmap.model.MarkerSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all marker sets and their markers.
 * Persists data to a JSON file in the plugin data folder.
 */
public class MarkerManager {

    private static final String DEFAULT_SET_ID = "markers";
    private static final String DEFAULT_SET_LABEL = "Markers";

    private final SmpDynmap plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();
    private File storageFile;

    public MarkerManager(SmpDynmap plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String filename = plugin.getDynmapConfig().getMarkersStorageFile();
        storageFile = new File(plugin.getDataFolder(), filename);

        // Always ensure a default marker set exists
        markerSets.putIfAbsent(DEFAULT_SET_ID,
                new MarkerSet(DEFAULT_SET_ID, DEFAULT_SET_LABEL, true));

        if (!storageFile.exists()) {
            save();
            return;
        }

        try (Reader r = new InputStreamReader(
                new FileInputStream(storageFile), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonArray sets = root.getAsJsonArray("sets");
            if (sets != null) {
                for (JsonElement setEl : sets) {
                    JsonObject setObj = setEl.getAsJsonObject();
                    String id = setObj.get("id").getAsString();
                    String label = setObj.get("label").getAsString();
                    boolean showLabels = setObj.has("showLabels")
                            && setObj.get("showLabels").getAsBoolean();
                    MarkerSet set = new MarkerSet(id, label, showLabels);

                    JsonArray markers = setObj.getAsJsonArray("markers");
                    if (markers != null) {
                        for (JsonElement markerEl : markers) {
                            JsonObject mObj = markerEl.getAsJsonObject();
                            DynmapMarker marker = new DynmapMarker(
                                    mObj.get("id").getAsString(),
                                    mObj.get("label").getAsString(),
                                    mObj.get("world").getAsString(),
                                    mObj.get("x").getAsDouble(),
                                    mObj.get("y").getAsDouble(),
                                    mObj.get("z").getAsDouble(),
                                    mObj.has("icon") ? mObj.get("icon").getAsString() : "default",
                                    id
                            );
                            set.addMarker(marker);
                        }
                    }
                    markerSets.put(id, set);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load markers from " + storageFile, e);
        }
    }

    public void save() {
        if (storageFile == null) return;
        storageFile.getParentFile().mkdirs();
        JsonObject root = new JsonObject();
        JsonArray sets = new JsonArray();

        for (MarkerSet set : markerSets.values()) {
            JsonObject setObj = new JsonObject();
            setObj.addProperty("id", set.getId());
            setObj.addProperty("label", set.getLabel());
            setObj.addProperty("showLabels", set.isShowLabels());

            JsonArray markers = new JsonArray();
            for (DynmapMarker marker : set.getMarkers()) {
                JsonObject mObj = new JsonObject();
                mObj.addProperty("id", marker.getId());
                mObj.addProperty("label", marker.getLabel());
                mObj.addProperty("world", marker.getWorld());
                mObj.addProperty("x", marker.getX());
                mObj.addProperty("y", marker.getY());
                mObj.addProperty("z", marker.getZ());
                mObj.addProperty("icon", marker.getIcon());
                markers.add(mObj);
            }
            setObj.add("markers", markers);
            sets.add(setObj);
        }
        root.add("sets", sets);

        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(storageFile), StandardCharsets.UTF_8)) {
            gson.toJson(root, w);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save markers", e);
        }
    }

    // ─── Marker Set API ────────────────────────────────────────────────────

    public MarkerSet createSet(String id, String label, boolean showLabels) {
        MarkerSet set = new MarkerSet(id, label, showLabels);
        markerSets.put(id, set);
        save();
        return set;
    }

    public boolean deleteSet(String id) {
        if (DEFAULT_SET_ID.equals(id)) return false;
        boolean removed = markerSets.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public MarkerSet getSet(String id) { return markerSets.get(id); }

    public Collection<MarkerSet> getAllSets() {
        return Collections.unmodifiableCollection(markerSets.values());
    }

    // ─── Marker API ────────────────────────────────────────────────────────

    public DynmapMarker addMarker(String setId, String markerId, String label,
                                  String world, double x, double y, double z,
                                  String icon) {
        MarkerSet set = markerSets.get(setId);
        if (set == null) return null;
        if (set.findMarker(markerId) != null) return null; // duplicate
        DynmapMarker marker = new DynmapMarker(markerId, label, world, x, y, z, icon, setId);
        set.addMarker(marker);
        save();
        return marker;
    }

    public boolean deleteMarker(String setId, String markerId) {
        MarkerSet set = markerSets.get(setId);
        if (set == null) return false;
        boolean removed = set.removeMarker(markerId);
        if (removed) save();
        return removed;
    }

    public DynmapMarker findMarker(String markerId) {
        for (MarkerSet set : markerSets.values()) {
            DynmapMarker m = set.findMarker(markerId);
            if (m != null) return m;
        }
        return null;
    }

    /** Return all markers for a given world, across all sets. */
    public List<DynmapMarker> getMarkersInWorld(String worldName) {
        List<DynmapMarker> result = new ArrayList<>();
        for (MarkerSet set : markerSets.values()) {
            for (DynmapMarker m : set.getMarkers()) {
                if (worldName.equals(m.getWorld())) result.add(m);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
