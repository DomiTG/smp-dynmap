package com.domitg.smpdynmap.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named set of markers (e.g. "Bases", "Points of Interest").
 */
public class MarkerSet {

    private final String id;
    private String label;
    private boolean showLabels;
    private int minimumZoom;
    private int priority;
    private final List<DynmapMarker> markers = new ArrayList<>();

    public MarkerSet(String id, String label, boolean showLabels) {
        this.id = id;
        this.label = label;
        this.showLabels = showLabels;
        this.minimumZoom = 0;
        this.priority = 0;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isShowLabels() { return showLabels; }
    public void setShowLabels(boolean showLabels) { this.showLabels = showLabels; }
    public int getMinimumZoom() { return minimumZoom; }
    public void setMinimumZoom(int minimumZoom) { this.minimumZoom = minimumZoom; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public List<DynmapMarker> getMarkers() {
        return Collections.unmodifiableList(markers);
    }

    public void addMarker(DynmapMarker marker) {
        markers.add(marker);
    }

    public boolean removeMarker(String markerId) {
        return markers.removeIf(m -> m.getId().equals(markerId));
    }

    public DynmapMarker findMarker(String markerId) {
        return markers.stream()
                .filter(m -> m.getId().equals(markerId))
                .findFirst()
                .orElse(null);
    }
}
