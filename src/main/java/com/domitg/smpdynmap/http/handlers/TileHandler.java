package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.renderer.TileRenderer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Serves pre-rendered tile PNG images.
 *
 * <p>URL pattern:
 * {@code /tiles/{worldName}/{mapType}/{tileX}_{tileZ}.png}          (zoom 0)
 * {@code /tiles/{worldName}/{mapType}/z/{tileX}_{tileZ}.png}        (zoom 1)
 * {@code /tiles/{worldName}/{mapType}/zz/{tileX}_{tileZ}.png}       (zoom 2)
 * etc.</p>
 */
public class TileHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final File tilesDir;
    private final boolean allowCors;

    public TileHandler(SmpDynmap plugin) {
        this.plugin = plugin;
        this.tilesDir = new File(plugin.getDataFolder(), "tiles");
        this.allowCors = plugin.getDynmapConfig().isAllowCors();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (HttpUtil.handleOptions(ex)) return;
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendError(ex, 405, "Method Not Allowed");
            return;
        }

        // Strip leading /tiles/
        String path = ex.getRequestURI().getPath();
        if (!path.startsWith("/tiles/")) {
            HttpUtil.sendError(ex, 400, "Bad Request");
            return;
        }
        String relative = path.substring("/tiles/".length());

        // relative is e.g. "world/flat/10_-5.png"  or  "world/flat/z/10_-5.png"
        File tileFile = new File(tilesDir, relative.replace('/', File.separatorChar));

        // Validate path doesn't escape tiles directory (path traversal prevention)
        if (!tileFile.getCanonicalPath().startsWith(tilesDir.getCanonicalPath())) {
            HttpUtil.sendError(ex, 403, "Forbidden");
            return;
        }

        if (!tileFile.exists()) {
            // Return a 1x1 transparent PNG as placeholder
            HttpUtil.sendBytes(ex, 200, EMPTY_TILE_PNG, "image/png", allowCors);
            return;
        }

        byte[] data = Files.readAllBytes(tileFile.toPath());
        HttpUtil.sendBytes(ex, 200, data, "image/png", allowCors);
    }

    // 1×1 transparent PNG (for tiles that haven't been rendered yet)
    private static final byte[] EMPTY_TILE_PNG = {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG sig
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,        // IHDR chunk length + type
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,        // 1x1
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4, (byte)0x89, // bit depth, color type, CRC
            0x00, 0x00, 0x00, 0x0B, 0x49, 0x44, 0x41, 0x54,        // IDAT chunk
            0x78, (byte)0x9C, 0x62, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, (byte)0xE2, 0x21, (byte)0xBC, 0x33,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82  // IEND
    };
}
