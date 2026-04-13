package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.DynmapMarker;
import com.domitg.smpdynmap.model.MarkerSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Serves marker data as JSON.
 *
 * <pre>
 * GET /up/markers/{worldName}          → all marker sets for that world
 * GET /up/markers/{worldName}/{setId}  → specific marker set
 * </pre>
 */
public class MarkerHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final boolean allowCors;

    public MarkerHandler(SmpDynmap plugin) {
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

        // Path: /up/markers/{worldName}[/{setId}]
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        // parts[0]="" [1]="up" [2]="markers" [3]=worldName [4]=setId (optional)

        if (parts.length < 4) {
            HttpUtil.sendError(ex, 400, "Bad Request: missing world name");
            return;
        }

        String worldName = parts[3];
        String setFilter = parts.length >= 5 ? parts[4] : null;

        JsonObject root = new JsonObject();
        root.addProperty("timestamp", System.currentTimeMillis());

        JsonArray sets = new JsonArray();
        for (MarkerSet set : plugin.getMarkerManager().getAllSets()) {
            if (setFilter != null && !set.getId().equals(setFilter)) continue;

            JsonObject setObj = new JsonObject();
            setObj.addProperty("id", set.getId());
            setObj.addProperty("label", set.getLabel());
            setObj.addProperty("showLabels", set.isShowLabels());
            setObj.addProperty("minzoom", set.getMinimumZoom());
            setObj.addProperty("priority", set.getPriority());

            JsonArray markers = new JsonArray();
            for (DynmapMarker marker : set.getMarkers()) {
                if (!marker.getWorld().equals(worldName)) continue;
                JsonObject mObj = new JsonObject();
                mObj.addProperty("id", marker.getId());
                mObj.addProperty("label", marker.getLabel());
                mObj.addProperty("world", marker.getWorld());
                mObj.addProperty("x", marker.getX());
                mObj.addProperty("y", marker.getY());
                mObj.addProperty("z", marker.getZ());
                mObj.addProperty("icon", marker.getIcon());
                mObj.addProperty("set", marker.getSetId());
                mObj.addProperty("type", "marker");
                markers.add(mObj);
            }
            setObj.add("markers", markers);
            sets.add(setObj);
        }
        root.add("sets", sets);

        HttpUtil.sendJson(ex, 200, root.toString(), allowCors);
    }
}
