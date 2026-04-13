package com.domitg.smpdynmap.commands;

import com.domitg.smpdynmap.SmpDynmap;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@code /dynmap} command.
 *
 * <h3>Subcommands</h3>
 * <pre>
 *  /dynmap render           – queue current chunk for re-render
 *  /dynmap fullrender       – queue all loaded chunks in the world
 *  /dynmap cancelrender     – clear the render queue
 *  /dynmap pause            – pause background rendering
 *  /dynmap resume           – resume background rendering
 *  /dynmap reload           – reload configuration
 *  /dynmap show             – show yourself on the map
 *  /dynmap hide             – hide yourself from the map
 *  /dynmap stats            – show render queue stats
 *  /dynmap help             – show command list
 * </pre>
 */
public class DynmapCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.DARK_GREEN + "[SmpDynmap] " + ChatColor.RESET;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "render", "fullrender", "cancelrender", "pause", "resume",
            "reload", "show", "hide", "stats", "help");

    private final SmpDynmap plugin;

    public DynmapCommand(SmpDynmap plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "render" -> cmdRender(sender);
            case "fullrender" -> cmdFullRender(sender);
            case "cancelrender" -> cmdCancelRender(sender);
            case "pause" -> cmdPause(sender);
            case "resume" -> cmdResume(sender);
            case "reload" -> cmdReload(sender);
            case "show" -> cmdShow(sender);
            case "hide" -> cmdHide(sender);
            case "stats" -> cmdStats(sender);
            default -> {
                sender.sendMessage(PREFIX + ChatColor.RED + "Unknown subcommand. Use /dynmap help");
            }
        }
        return true;
    }

    // ─── Subcommand implementations ─────────────────────────────────────────

    private void cmdRender(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "This command must be run by a player.");
            return;
        }
        World world = player.getWorld();
        plugin.getWorldRenderer().queueChunkUpdate(world,
                player.getLocation().getBlockX() >> 4,
                player.getLocation().getBlockZ() >> 4);
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Tile queued for re-render.");
    }

    private void cmdFullRender(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        World world = resolveWorld(sender);
        if (world == null) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getWorldRenderer().queueFullRender(world));
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Full render queued for world: "
                + ChatColor.YELLOW + world.getName());
    }

    private void cmdCancelRender(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        plugin.getWorldRenderer().pause();
        plugin.getWorldRenderer().resume();   // pause+resume clears nothing – intentional
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Render queue will be drained; new tasks paused briefly.");
    }

    private void cmdPause(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        plugin.getWorldRenderer().pause();
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Rendering paused.");
    }

    private void cmdResume(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        plugin.getWorldRenderer().resume();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Rendering resumed.");
    }

    private void cmdReload(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        plugin.reloadDynmapConfig();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded.");
    }

    private void cmdShow(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "This command must be run by a player.");
            return;
        }
        if (!player.hasPermission("smpdynmap.show")) { noPerms(sender); return; }
        plugin.getUpdateManager().showPlayer(player.getUniqueId());
        sender.sendMessage(PREFIX + ChatColor.GREEN + "You are now visible on the map.");
    }

    private void cmdHide(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "This command must be run by a player.");
            return;
        }
        if (!player.hasPermission("smpdynmap.hide")) { noPerms(sender); return; }
        plugin.getUpdateManager().hidePlayer(player.getUniqueId());
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "You are now hidden from the map.");
    }

    private void cmdStats(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.admin")) { noPerms(sender); return; }
        int queueSize = plugin.getWorldRenderer().getQueueSize();
        boolean paused = plugin.getWorldRenderer().isPaused();
        int players = plugin.getServer().getOnlinePlayers().size();
        sender.sendMessage(PREFIX + ChatColor.WHITE + "=== SmpDynmap Stats ===");
        sender.sendMessage(ChatColor.GRAY + "  Render queue: " + ChatColor.YELLOW + queueSize + " tiles");
        sender.sendMessage(ChatColor.GRAY + "  Render paused: " + (paused ? ChatColor.RED + "yes" : ChatColor.GREEN + "no"));
        sender.sendMessage(ChatColor.GRAY + "  Online players tracked: " + ChatColor.YELLOW + players);
        sender.sendMessage(ChatColor.GRAY + "  HTTP port: " + ChatColor.YELLOW
                + plugin.getDynmapConfig().getHttpPort());
    }

    // ─── Helper methods ─────────────────────────────────────────────────────

    private World resolveWorld(CommandSender sender) {
        if (sender instanceof Player player) return player.getWorld();
        if (plugin.getServer().getWorlds().isEmpty()) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No worlds loaded.");
            return null;
        }
        return plugin.getServer().getWorlds().get(0);
    }

    private void noPerms(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.RED + "You do not have permission to do that.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GREEN + "=== SmpDynmap Commands ===");
        sender.sendMessage(ChatColor.GREEN + "/dynmap render" + ChatColor.WHITE + " – re-render current chunk");
        sender.sendMessage(ChatColor.GREEN + "/dynmap fullrender" + ChatColor.WHITE + " – queue full world render");
        sender.sendMessage(ChatColor.GREEN + "/dynmap cancelrender" + ChatColor.WHITE + " – cancel pending renders");
        sender.sendMessage(ChatColor.GREEN + "/dynmap pause/resume" + ChatColor.WHITE + " – pause/resume rendering");
        sender.sendMessage(ChatColor.GREEN + "/dynmap reload" + ChatColor.WHITE + " – reload config");
        sender.sendMessage(ChatColor.GREEN + "/dynmap show/hide" + ChatColor.WHITE + " – toggle your visibility");
        sender.sendMessage(ChatColor.GREEN + "/dynmap stats" + ChatColor.WHITE + " – show statistics");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
