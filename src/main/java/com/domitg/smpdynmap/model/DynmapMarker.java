package com.domitg.smpdynmap.model;

/**
 * An individual marker (point of interest) placed on the dynmap.
 */
public class DynmapMarker {

    private final String id;
    private String label;
    private String world;
    private double x;
    private double y;
    private double z;
    private String icon;
    private String setId;
    private final long createdAt;

    public DynmapMarker(String id, String label, String world,
                        double x, double y, double z,
                        String icon, String setId) {
        this.id = id;
        this.label = label;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.icon = icon;
        this.setId = setId;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }
    public long getCreatedAt() { return createdAt; }
}
