package com.domitg.smpdynmap.http;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.http.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Wraps the JDK built-in {@link HttpServer} and registers all route handlers.
 *
 * <h3>Exposed endpoints</h3>
 * <pre>
 *  GET  /                             → index.html
 *  GET  /tiles/{world}/{map}/...      → PNG tile images
 *  GET  /up/configuration             → server/world config JSON
 *  GET  /up/world/{world}/{timestamp} → player + chat update JSON
 *  GET  /up/markers/{world}           → marker data JSON
 *  POST /up/sendchat                  → relay a web-chat message
 *  GET  /web/...                      → static files from web/ resource dir
 * </pre>
 */
public class DynmapHttpServer {

    private final SmpDynmap plugin;
    private HttpServer server;

    public DynmapHttpServer(SmpDynmap plugin) {
        this.plugin = plugin;
    }

    public void start() throws IOException {
        String address = plugin.getDynmapConfig().getHttpAddress();
        int port = plugin.getDynmapConfig().getHttpPort();
        int maxConn = plugin.getDynmapConfig().getMaxConnections();

        server = HttpServer.create(new InetSocketAddress(address, port), maxConn);
        server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "smpdynmap-http");
            t.setDaemon(true);
            return t;
        }));

        // Static / web root
        server.createContext("/", new StaticFileHandler(plugin));

        // Tile images
        server.createContext("/tiles/", new TileHandler(plugin));

        // Update feeds
        server.createContext("/up/configuration", new ConfigurationHandler(plugin));
        server.createContext("/up/world/", new UpdateHandler(plugin));
        server.createContext("/up/markers/", new MarkerHandler(plugin));
        server.createContext("/up/sendchat", new SendChatHandler(plugin));

        server.start();
        plugin.getLogger().info("Dynmap web server started on "
                + address + ":" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(2);
            plugin.getLogger().info("Dynmap web server stopped.");
        }
    }
}
