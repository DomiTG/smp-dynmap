package com.domitg.smpdynmap;

import com.domitg.smpdynmap.commands.DMarkerCommand;
import com.domitg.smpdynmap.commands.DynmapCommand;
import com.domitg.smpdynmap.events.MapEventListener;
import com.domitg.smpdynmap.http.DynmapHttpServer;
import com.domitg.smpdynmap.markers.MarkerManager;
import com.domitg.smpdynmap.renderer.WorldRenderer;
import com.domitg.smpdynmap.update.UpdateManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Main entry point for the SmpDynmap plugin.
 *
 * <h3>Startup sequence</h3>
 * <ol>
 *   <li>Save default config</li>
 *   <li>Load {@link DynmapConfig}</li>
 *   <li>Initialize {@link UpdateManager} (player/chat tracking)</li>
 *   <li>Initialize {@link MarkerManager} and load markers from disk</li>
 *   <li>Initialize {@link WorldRenderer} (async tile render pool)</li>
 *   <li>Start the built-in {@link DynmapHttpServer}</li>
 *   <li>Register Bukkit event listener</li>
 *   <li>Register commands</li>
 *   <li>Start the periodic player-update scheduler</li>
 * </ol>
 */
public final class SmpDynmap extends JavaPlugin {

    private DynmapConfig dynmapConfig;
    private UpdateManager updateManager;
    private MarkerManager markerManager;
    private WorldRenderer worldRenderer;
    private DynmapHttpServer httpServer;

    @Override
    public void onEnable() {
        // 1. Config
        saveDefaultConfig();
        dynmapConfig = new DynmapConfig(this);

        // 2. Core subsystems
        updateManager = new UpdateManager(this);
        markerManager = new MarkerManager(this);
        markerManager.load();

        // 3. Renderer
        worldRenderer = new WorldRenderer(this);

        // 4. HTTP server
        if (dynmapConfig.isHttpEnabled()) {
            httpServer = new DynmapHttpServer(this);
            try {
                httpServer.start();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE,
                        "Failed to start dynmap HTTP server on port "
                                + dynmapConfig.getHttpPort(), e);
            }
        } else {
            getLogger().info("HTTP server is disabled in config.");
        }

        // 5. Events
        getServer().getPluginManager().registerEvents(new MapEventListener(this), this);

        // 6. Commands
        DynmapCommand dynmapCmd = new DynmapCommand(this);
        getCommand("dynmap").setExecutor(dynmapCmd);
        getCommand("dynmap").setTabCompleter(dynmapCmd);

        DMarkerCommand dmarkerCmd = new DMarkerCommand(this);
        getCommand("dmarker").setExecutor(dmarkerCmd);
        getCommand("dmarker").setTabCompleter(dmarkerCmd);

        // 7. Periodic scheduler – refresh player data every update period
        int periodTicks = dynmapConfig.getUpdatePeriodTicks();
        getServer().getScheduler().runTaskTimer(this,
                updateManager::refreshPlayers, 20L, periodTicks);

        // 8. Queue a full render for all already-loaded worlds on startup
        getServer().getScheduler().runTaskLater(this, () -> {
            for (org.bukkit.World world : getServer().getWorlds()) {
                if (dynmapConfig.isWorldEnabled(world.getName())) {
                    getServer().getScheduler().runTaskAsynchronously(this, () ->
                            worldRenderer.queueFullRender(world));
                }
            }
        }, 40L);  // 2 seconds after startup

        getLogger().info("SmpDynmap v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (worldRenderer != null) worldRenderer.shutdown();
        if (httpServer != null) httpServer.stop();
        if (markerManager != null) markerManager.save();
        getLogger().info("SmpDynmap disabled.");
    }

    // ─── Public accessors ─────────────────────────────────────────────────

    public DynmapConfig getDynmapConfig() { return dynmapConfig; }

    public UpdateManager getUpdateManager() { return updateManager; }

    public MarkerManager getMarkerManager() { return markerManager; }

    public WorldRenderer getWorldRenderer() { return worldRenderer; }

    // ─── Reload ───────────────────────────────────────────────────────────

    public void reloadDynmapConfig() {
        reloadConfig();
        // dynmapConfig reads live from getConfig() so no re-init needed
        getLogger().info("SmpDynmap configuration reloaded.");
    }
}
