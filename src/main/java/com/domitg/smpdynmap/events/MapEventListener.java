package com.domitg.smpdynmap.events;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.ChatMessage;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;

/**
 * Listens to Bukkit events and notifies the render queue and update manager
 * so that map tiles and player data stay current.
 */
public class MapEventListener implements Listener {

    private final SmpDynmap plugin;

    public MapEventListener(SmpDynmap plugin) {
        this.plugin = plugin;
    }

    // ─── Block events ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.getWorldRenderer().queueBlockUpdate(
                event.getBlock().getWorld(),
                event.getBlock().getX(),
                event.getBlock().getZ()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getWorldRenderer().queueBlockUpdate(
                event.getBlock().getWorld(),
                event.getBlock().getX(),
                event.getBlock().getZ()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        plugin.getWorldRenderer().queueBlockUpdate(
                event.getBlock().getWorld(),
                event.getBlock().getX(),
                event.getBlock().getZ()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        plugin.getWorldRenderer().queueBlockUpdate(
                event.getBlock().getWorld(),
                event.getBlock().getX(),
                event.getBlock().getZ()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeavesDecay(LeavesDecayEvent event) {
        plugin.getWorldRenderer().queueBlockUpdate(
                event.getBlock().getWorld(),
                event.getBlock().getX(),
                event.getBlock().getZ()
        );
    }

    // ─── Chunk events ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            // Only queue newly generated chunks to avoid rendering everything on load
            plugin.getWorldRenderer().queueChunkUpdate(
                    event.getWorld(),
                    event.getChunk().getX(),
                    event.getChunk().getZ()
            );
        }
    }

    // ─── Player events ─────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getUpdateManager().refreshPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getUpdateManager().refreshPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        plugin.getUpdateManager().refreshPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getDynmapConfig().isChatEnabled()) return;
        plugin.getUpdateManager().addChatMessage(
                new ChatMessage(event.getPlayer().getName(), event.getMessage(), "global")
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getUpdateManager().refreshPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getUpdateManager().refreshPlayers();
    }

    // ─── World events ──────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        // Queue all loaded chunks for the newly loaded world
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getWorldRenderer().queueFullRender(event.getWorld()));
    }
}
