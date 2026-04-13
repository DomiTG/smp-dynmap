package com.domitg.smpdynmap.model;

/**
 * Snapshot of a player's position and stats, sent to web viewers.
 */
public class PlayerInfo {

    private final String name;
    private final String displayName;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final double health;
    private final int armor;
    private final boolean hidden;
    private final long timestamp;

    public PlayerInfo(String name, String displayName, String world,
                      double x, double y, double z,
                      float yaw, float pitch,
                      double health, int armor,
                      boolean hidden) {
        this.name = name;
        this.displayName = displayName;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.health = health;
        this.armor = armor;
        this.hidden = hidden;
        this.timestamp = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public double getHealth() { return health; }
    public int getArmor() { return armor; }
    public boolean isHidden() { return hidden; }
    public long getTimestamp() { return timestamp; }
}
