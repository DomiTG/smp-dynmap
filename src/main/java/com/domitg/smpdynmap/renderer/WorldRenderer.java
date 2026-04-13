package com.domitg.smpdynmap.renderer;

import com.domitg.smpdynmap.SmpDynmap;
import org.bukkit.World;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages the per-world render queue and the background render thread pool.
 *
 * <p>Chunks are queued for rendering whenever they are modified or loaded.
 * The background threads drain the queue using {@link TileRenderer}.</p>
 */
public class WorldRenderer {

    private final SmpDynmap plugin;
    private final TileRenderer tileRenderer;
    private final ExecutorService executor;
    private final Set<String> renderQueue;   // "worldName:tileX:tileZ"
    private final int maxQueue;
    private final int zoomLevels;
    private volatile boolean paused = false;

    public WorldRenderer(SmpDynmap plugin) {
        this.plugin = plugin;
        this.tileRenderer = new TileRenderer(plugin,
                plugin.getDynmapConfig().isSlopeShading());
        int threads = plugin.getDynmapConfig().getRenderThreads();
        this.executor = Executors.newFixedThreadPool(Math.max(1, threads), r -> {
            Thread t = new Thread(r, "smpdynmap-render");
            t.setDaemon(true);
            return t;
        });
        this.maxQueue = plugin.getDynmapConfig().getMaxTilesInQueue();
        this.zoomLevels = plugin.getDynmapConfig().getZoomLevels();
        // Synchronized set to avoid duplicates
        this.renderQueue = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    // ─── Queue management ──────────────────────────────────────────────────

    /**
     * Queues the tile that contains the given block coordinates for
     * (re)rendering.
     */
    public void queueBlockUpdate(World world, int blockX, int blockZ) {
        if (!plugin.getDynmapConfig().isWorldEnabled(world.getName())) return;
        int tileX = Math.floorDiv(blockX, TileRenderer.TILE_SIZE);
        int tileZ = Math.floorDiv(blockZ, TileRenderer.TILE_SIZE);
        queueTile(world, tileX, tileZ);
    }

    /**
     * Queues the tile that contains the given chunk for (re)rendering.
     */
    public void queueChunkUpdate(World world, int chunkX, int chunkZ) {
        if (!plugin.getDynmapConfig().isWorldEnabled(world.getName())) return;
        // A chunk spans 16 blocks; find which tile(s) it belongs to.
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        int tileX = Math.floorDiv(blockX, TileRenderer.TILE_SIZE);
        int tileZ = Math.floorDiv(blockZ, TileRenderer.TILE_SIZE);
        queueTile(world, tileX, tileZ);

        // A chunk on the boundary of a tile can affect a neighbouring tile
        if ((blockX & (TileRenderer.TILE_SIZE - 1)) == 0) {
            queueTile(world, tileX - 1, tileZ);
        }
        if ((blockZ & (TileRenderer.TILE_SIZE - 1)) == 0) {
            queueTile(world, tileX, tileZ - 1);
        }
    }

    /** Queue every loaded chunk in the world for a full re-render. */
    public void queueFullRender(World world) {
        if (!plugin.getDynmapConfig().isWorldEnabled(world.getName())) return;
        plugin.getLogger().info("Queuing full render for world: " + world.getName());
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            queueChunkUpdate(world, chunk.getX(), chunk.getZ());
        }
    }

    private void queueTile(World world, int tileX, int tileZ) {
        if (paused) return;
        if (renderQueue.size() >= maxQueue) return;
        String key = world.getName() + ":" + tileX + ":" + tileZ;
        if (renderQueue.add(key)) {
            executor.submit(() -> processKey(key));
        }
    }

    private void processKey(String key) {
        if (paused) {
            renderQueue.remove(key);
            return;
        }
        String[] parts = key.split(":");
        if (parts.length != 3) return;
        String worldName = parts[0];
        int tileX = Integer.parseInt(parts[1]);
        int tileZ = Integer.parseInt(parts[2]);

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            renderQueue.remove(key);
            return;
        }

        File tilesDir = new File(plugin.getDataFolder(), "tiles");
        // Render for each enabled map type of this world
        for (String mapType : plugin.getDynmapConfig().getMapTypes(worldName)) {
            try {
                tileRenderer.renderTile(world, tileX, tileZ, mapType, zoomLevels, tilesDir);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Error rendering tile " + tileX + "," + tileZ, e);
            }
        }
        renderQueue.remove(key);
    }

    // ─── Controls ──────────────────────────────────────────────────────────

    public void pause() { paused = true; }

    public void resume() { paused = false; }

    public boolean isPaused() { return paused; }

    public int getQueueSize() { return renderQueue.size(); }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
