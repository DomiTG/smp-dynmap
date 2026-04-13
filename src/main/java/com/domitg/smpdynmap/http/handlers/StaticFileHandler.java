package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Serves static files from:
 * <ol>
 *   <li>The {@code web/} sub-directory of the plugin data folder (user-overridable)</li>
 *   <li>The {@code /web/} classpath resource directory bundled inside the JAR</li>
 * </ol>
 * Requests to {@code /} are redirected to {@code /index.html}.
 */
public class StaticFileHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final File webRoot;
    private final boolean allowCors;

    public StaticFileHandler(SmpDynmap plugin) {
        this.plugin = plugin;
        this.webRoot = new File(plugin.getDataFolder(),
                plugin.getDynmapConfig().getWebRoot());
        this.allowCors = plugin.getDynmapConfig().isAllowCors();
        webRoot.mkdirs();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (HttpUtil.handleOptions(ex)) return;

        String path = ex.getRequestURI().getPath();

        // Don't intercept API routes
        if (path.startsWith("/tiles/") || path.startsWith("/up/")) {
            HttpUtil.sendError(ex, 404, "Not found");
            return;
        }

        if ("/".equals(path) || path.isEmpty()) path = "/index.html";

        // 1. Try filesystem web root
        File file = new File(webRoot, path.replace('/', File.separatorChar));
        if (file.exists() && file.isFile() && file.getCanonicalPath().startsWith(webRoot.getCanonicalPath())) {
            byte[] data = Files.readAllBytes(file.toPath());
            HttpUtil.sendBytes(ex, 200, data, HttpUtil.mimeType(path), allowCors);
            return;
        }

        // 2. Try bundled JAR resources (web/ directory)
        String resourcePath = "/web" + path;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                byte[] data = is.readAllBytes();
                HttpUtil.sendBytes(ex, 200, data, HttpUtil.mimeType(path), allowCors);
                return;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading resource: " + resourcePath, e);
        }

        HttpUtil.sendError(ex, 404, "File not found: " + path);
    }
}
