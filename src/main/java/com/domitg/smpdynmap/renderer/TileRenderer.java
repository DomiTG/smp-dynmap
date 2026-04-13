package com.domitg.smpdynmap.renderer;

import com.domitg.smpdynmap.SmpDynmap;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Renders a single 128×128 map tile (top-down view) from chunk snapshot data.
 *
 * <p>Coordinate conventions:
 * <ul>
 *   <li>Tile (tileX, tileZ) covers world blocks
 *       [tileX*TILE_SIZE … tileX*TILE_SIZE + TILE_SIZE – 1] × (same for Z)</li>
 *   <li>Each tile is {@link #TILE_SIZE}×{@link #TILE_SIZE} pixels at zoom 0
 *       (one pixel = one block column).</li>
 *   <li>Higher zoom levels are produced by down-sampling: zoom 1 = 2× zoom-out,
 *       zoom 2 = 4× zoom-out, etc.</li>
 * </ul>
 * </p>
 */
public class TileRenderer {

    public static final int TILE_SIZE = 128;

    private final SmpDynmap plugin;
    private final boolean slopeShading;

    public TileRenderer(SmpDynmap plugin, boolean slopeShading) {
        this.plugin = plugin;
        this.slopeShading = slopeShading;
    }

    // ─── Public API ────────────────────────────────────────────────────────

    /**
     * Renders the tile at (tileX, tileZ) for the given world and saves it to
     * {@code outputFile}.  Also updates every parent zoom tile that contains
     * this tile.
     *
     * <p>This method MUST be called from an async thread.</p>
     */
    public boolean renderTile(World world, int tileX, int tileZ,
                              String mapType, int zoomLevels, File tilesDir) {
        try {
            BufferedImage img = renderBase(world, tileX, tileZ);
            if (img == null) return false;

            // Save zoom-0 tile
            File z0file = getTileFile(tilesDir, world.getName(), mapType, 0, tileX, tileZ);
            savePng(img, z0file);

            // Build / refresh parent zoom tiles
            refreshZoomParents(img, tilesDir, world.getName(), mapType, tileX, tileZ, zoomLevels);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to render tile " + tileX + "," + tileZ
                            + " for world " + world.getName(), e);
            return false;
        }
    }

    // ─── Core rendering ────────────────────────────────────────────────────

    /**
     * Produces a full-resolution BufferedImage for the tile using chunk
     * snapshots obtained from the main thread via a synchronous Bukkit
     * callback.
     */
    private BufferedImage renderBase(World world, int tileX, int tileZ) {
        int startBlockX = tileX * TILE_SIZE;
        int startBlockZ = tileZ * TILE_SIZE;

        // The tile spans up to 8×8 = 64 chunks (TILE_SIZE/16 = 8)
        int chunksPerSide = TILE_SIZE / 16;   // 8
        ChunkSnapshot[][] snapshots = new ChunkSnapshot[chunksPerSide + 1][chunksPerSide + 1];

        // Capture snapshots on the main thread
        try {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int startChunkX = startBlockX >> 4;
                    int startChunkZ = startBlockZ >> 4;
                    for (int cx = 0; cx <= chunksPerSide; cx++) {
                        for (int cz = 0; cz <= chunksPerSide; cz++) {
                            int chunkX = startChunkX + cx;
                            int chunkZ = startChunkZ + cz;
                            if (world.isChunkLoaded(chunkX, chunkZ)) {
                                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                                snapshots[cx][cz] = chunk.getChunkSnapshot(true, false, false);
                            } else {
                                // Force-load briefly for rendering, then unload
                                world.loadChunk(chunkX, chunkZ, false);
                                if (world.isChunkLoaded(chunkX, chunkZ)) {
                                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                                    snapshots[cx][cz] = chunk.getChunkSnapshot(true, false, false);
                                    world.unloadChunkRequest(chunkX, chunkZ);
                                }
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
            boolean completed = latch.await(
                    plugin.getDynmapConfig().getChunkLoadTimeout(), java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                plugin.getLogger().warning("Chunk snapshot timeout for tile " + tileX + "," + tileZ);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        // Render pixels
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        // Pre-compute a height grid (one extra row/column for slope shading)
        int[][] heights = new int[TILE_SIZE + 1][TILE_SIZE + 1];
        Color[][] colors = new Color[TILE_SIZE + 1][TILE_SIZE + 1];

        for (int px = 0; px <= TILE_SIZE; px++) {
            for (int pz = 0; pz <= TILE_SIZE; pz++) {
                int blockX = startBlockX + px;
                int blockZ = startBlockZ + pz;
                int cx = (blockX - startBlockX) / 16;
                int cz = (blockZ - startBlockZ) / 16;
                int bx = (blockX & 15);
                int bz = (blockZ & 15);

                // Clamp snapshot indices
                cx = Math.min(cx, chunksPerSide);
                cz = Math.min(cz, chunksPerSide);

                ChunkSnapshot snap = snapshots[cx][cz];
                if (snap == null) {
                    heights[px][pz] = minY;
                    colors[px][pz] = new Color(0x20, 0x20, 0x20);
                    continue;
                }

                int topY = getTopSolidY(snap, bx, bz, minY, maxY);
                heights[px][pz] = topY;
                colors[px][pz] = getColumnColor(snap, bx, bz, topY, minY);
            }
        }

        // Write pixels with optional slope shading
        for (int px = 0; px < TILE_SIZE; px++) {
            for (int pz = 0; pz < TILE_SIZE; pz++) {
                Color base = colors[px][pz];
                if (base.getAlpha() == 0) {
                    img.setRGB(px, pz, 0x00000000);
                    continue;
                }

                int rgb;
                if (slopeShading) {
                    rgb = applySlope(base, heights, px, pz);
                } else {
                    rgb = base.getRGB();
                }
                img.setRGB(px, pz, rgb);
            }
        }

        return img;
    }

    /** Find the topmost non-transparent block Y in this column. */
    private int getTopSolidY(ChunkSnapshot snap, int bx, int bz, int minY, int maxY) {
        int top = snap.getHighestBlockYAt(bx, bz);
        // Walk down until we find a non-transparent block
        for (int y = Math.min(top, maxY - 1); y >= minY; y--) {
            Material m = snap.getBlockType(bx, y, bz);
            if (!BlockColorMapper.isTransparent(m)) return y;
        }
        return minY;
    }

    /** Get the colour of the topmost solid block, blending water/glass. */
    private Color getColumnColor(ChunkSnapshot snap, int bx, int bz,
                                 int topY, int minY) {
        // Walk down collecting semi-transparent layers (water, glass…)
        List<Color> layers = new ArrayList<>();
        for (int y = Math.min(topY + 1, snap.getHighestBlockYAt(bx, bz)); y >= minY; y--) {
            Material m = snap.getBlockType(bx, y, bz);
            Color c = BlockColorMapper.getColor(m);
            if (c == BlockColorMapper.TRANSPARENT || c.getAlpha() == 0) continue;
            layers.add(c);
            if (c.getAlpha() == 255) break; // opaque — stop
        }
        if (layers.isEmpty()) return new Color(0x20, 0x20, 0x20);

        // Composite from bottom to top (Porter-Duff over)
        Color result = layers.get(layers.size() - 1);
        for (int i = layers.size() - 2; i >= 0; i--) {
            result = blend(layers.get(i), result);
        }
        return result;
    }

    /** Alpha-composite {@code top} over {@code bottom}. */
    private Color blend(Color top, Color bottom) {
        float a = top.getAlpha() / 255f;
        int r = (int) (top.getRed() * a + bottom.getRed() * (1 - a));
        int g = (int) (top.getGreen() * a + bottom.getGreen() * (1 - a));
        int b = (int) (top.getBlue() * a + bottom.getBlue() * (1 - a));
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    /**
     * Apply slope-based directional shading.
     * Light source is from the north-west / above.
     */
    private int applySlope(Color base, int[][] heights, int px, int pz) {
        int h  = heights[px][pz];
        int hE = heights[Math.min(px + 1, TILE_SIZE)][pz];
        int hS = heights[px][Math.min(pz + 1, TILE_SIZE)];

        // Normal vector components
        float nx = h - hE;
        float nz = h - hS;
        float ny = 2.0f;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len; ny /= len; nz /= len;

        // Light direction (from north-west above)
        float lx = 0.6f, ly = 0.8f, lz = 0.6f;
        float llen = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= llen; ly /= llen; lz /= llen;

        float dot = nx * lx + ny * ly + nz * lz;
        float shade = 0.65f + dot * 0.45f;   // range ≈ 0.2 … 1.1
        shade = Math.max(0.25f, Math.min(1.15f, shade));

        int r = clamp((int) (base.getRed() * shade));
        int g = clamp((int) (base.getGreen() * shade));
        int b = clamp((int) (base.getBlue() * shade));
        return (base.getAlpha() << 24) | (r << 16) | (g << 8) | b;
    }

    // ─── Zoom hierarchy ────────────────────────────────────────────────────

    /**
     * After saving a base (zoom-0) tile, merge it into the parent zoom tiles
     * (zoom 1 = 2× zoom-out, zoom 2 = 4× zoom-out, …).
     */
    private void refreshZoomParents(BufferedImage baseTile,
                                    File tilesDir, String worldName, String mapType,
                                    int tileX, int tileZ, int maxZoom) throws IOException {
        BufferedImage current = baseTile;
        int curX = tileX;
        int curZ = tileZ;

        for (int zoom = 1; zoom <= maxZoom; zoom++) {
            int parentX = Math.floorDiv(curX, 2);
            int parentZ = Math.floorDiv(curZ, 2);

            File parentFile = getTileFile(tilesDir, worldName, mapType, zoom, parentX, parentZ);
            BufferedImage parentImg = loadOrCreate(parentFile);

            // Position of current tile within the 2×2 parent quadrant
            int quadX = Math.floorMod(curX, 2);  // 0 or 1
            int quadZ = Math.floorMod(curZ, 2);  // 0 or 1
            int destX = quadX * (TILE_SIZE / 2);
            int destZ = quadZ * (TILE_SIZE / 2);

            // Down-sample current tile to TILE_SIZE/2 × TILE_SIZE/2 and paste
            BufferedImage half = downSample(current);
            parentImg.createGraphics().drawImage(half, destX, destZ, null);

            savePng(parentImg, parentFile);

            curX = parentX;
            curZ = parentZ;
            current = parentImg;
        }
    }

    /** Down-sample a TILE_SIZE×TILE_SIZE image to (TILE_SIZE/2)×(TILE_SIZE/2). */
    private BufferedImage downSample(BufferedImage src) {
        int half = TILE_SIZE / 2;
        BufferedImage dst = new BufferedImage(half, half, BufferedImage.TYPE_INT_ARGB);
        for (int px = 0; px < half; px++) {
            for (int pz = 0; pz < half; pz++) {
                // Average 2×2 block of pixels
                int rgb00 = src.getRGB(px * 2,     pz * 2);
                int rgb10 = src.getRGB(px * 2 + 1, pz * 2);
                int rgb01 = src.getRGB(px * 2,     pz * 2 + 1);
                int rgb11 = src.getRGB(px * 2 + 1, pz * 2 + 1);
                dst.setRGB(px, pz, avgRgb(rgb00, rgb10, rgb01, rgb11));
            }
        }
        return dst;
    }

    private int avgRgb(int a, int b, int c, int d) {
        int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF) + ((c >> 16) & 0xFF) + ((d >> 16) & 0xFF)) / 4;
        int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF) + ((c >> 8) & 0xFF) + ((d >> 8) & 0xFF)) / 4;
        int bl = ((a & 0xFF) + (b & 0xFF) + (c & 0xFF) + (d & 0xFF)) / 4;
        int alpha = (((a >> 24) & 0xFF) + ((b >> 24) & 0xFF) + ((c >> 24) & 0xFF) + ((d >> 24) & 0xFF)) / 4;
        return (alpha << 24) | (r << 16) | (g << 8) | bl;
    }

    // ─── File helpers ───────────────────────────────────────────────────────

    /**
     * Returns the file path for a tile.
     * Path: {@code tilesDir/{worldName}/{mapType}/z{zoom}/{tileX}_{tileZ}.png}
     * For zoom 0 the "z0" prefix is omitted for compact storage:
     * {@code tilesDir/{worldName}/{mapType}/{tileX}_{tileZ}.png}
     */
    public static File getTileFile(File tilesDir, String worldName, String mapType,
                                   int zoom, int tileX, int tileZ) {
        File dir;
        if (zoom == 0) {
            dir = new File(tilesDir, worldName + File.separator + mapType);
        } else {
            dir = new File(tilesDir, worldName + File.separator + mapType
                    + File.separator + "z".repeat(zoom));
        }
        dir.mkdirs();
        return new File(dir, tileX + "_" + tileZ + ".png");
    }

    private void savePng(BufferedImage img, File file) throws IOException {
        file.getParentFile().mkdirs();
        ImageIO.write(img, "PNG", file);
    }

    private BufferedImage loadOrCreate(File file) {
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) { /* fall through */ }
        }
        return new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
