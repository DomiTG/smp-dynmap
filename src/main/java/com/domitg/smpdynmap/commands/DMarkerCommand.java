package com.domitg.smpdynmap.commands;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.DynmapMarker;
import com.domitg.smpdynmap.model.MarkerSet;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the {@code /dmarker} command.
 *
 * <h3>Subcommands</h3>
 * <pre>
 *  /dmarker add &lt;id&gt; [label] [icon]           – add a marker at your position
 *  /dmarker delete &lt;id&gt;                        – delete a marker by id
 *  /dmarker list                                – list all markers
 *  /dmarker set &lt;id&gt; label|icon &lt;value&gt;       – update marker field
 *  /dmarker addset &lt;id&gt; [label]               – create a new marker set
 *  /dmarker deleteset &lt;id&gt;                     – delete a marker set
 *  /dmarker listsets                            – list all marker sets
 *  /dmarker help                                – show help
 * </pre>
 */
public class DMarkerCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.GOLD + "[Markers] " + ChatColor.RESET;
    private static final String DEFAULT_SET = "markers";
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "add", "delete", "list", "set", "addset", "deleteset", "listsets", "help");

    private final SmpDynmap plugin;

    public DMarkerCommand(SmpDynmap plugin) {
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
            case "add"       -> cmdAdd(sender, args);
            case "delete"    -> cmdDelete(sender, args);
            case "list"      -> cmdList(sender);
            case "set"       -> cmdSet(sender, args);
            case "addset"    -> cmdAddSet(sender, args);
            case "deleteset" -> cmdDeleteSet(sender, args);
            case "listsets"  -> cmdListSets(sender);
            default -> sender.sendMessage(PREFIX + ChatColor.RED + "Unknown subcommand. Use /dmarker help");
        }
        return true;
    }

    // ─── Subcommands ────────────────────────────────────────────────────────

    private void cmdAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "This command must be run by a player."); return;
        }
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dmarker add <id> [label] [icon]");
            return;
        }
        String id = args[1];
        String label = args.length >= 3 ? args[2] : id;
        String icon = args.length >= 4 ? args[3] : "default";

        if (plugin.getMarkerManager().findMarker(id) != null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "A marker with id '" + id + "' already exists.");
            return;
        }

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        String world = player.getWorld().getName();

        DynmapMarker marker = plugin.getMarkerManager().addMarker(
                DEFAULT_SET, id, label, world, x, y, z, icon);
        if (marker == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Failed to add marker (set not found or duplicate).");
            return;
        }
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Marker '" + label + "' added at "
                + String.format("%.1f, %.1f, %.1f", x, y, z) + " in " + world);
    }

    private void cmdDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dmarker delete <id>");
            return;
        }
        String id = args[1];
        // Search all sets
        for (MarkerSet set : plugin.getMarkerManager().getAllSets()) {
            if (plugin.getMarkerManager().deleteMarker(set.getId(), id)) {
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Marker '" + id + "' deleted.");
                return;
            }
        }
        sender.sendMessage(PREFIX + ChatColor.RED + "No marker found with id '" + id + "'.");
    }

    private void cmdList(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        int total = 0;
        sender.sendMessage(ChatColor.GOLD + "=== Markers ===");
        for (MarkerSet set : plugin.getMarkerManager().getAllSets()) {
            for (DynmapMarker m : set.getMarkers()) {
                sender.sendMessage(ChatColor.YELLOW + m.getId() + ChatColor.WHITE + " – "
                        + m.getLabel() + ChatColor.GRAY + " [" + m.getWorld() + " "
                        + String.format("%.0f,%.0f,%.0f", m.getX(), m.getY(), m.getZ()) + "]");
                total++;
            }
        }
        if (total == 0) sender.sendMessage(ChatColor.GRAY + "No markers defined.");
        else sender.sendMessage(ChatColor.GRAY + "Total: " + total);
    }

    private void cmdSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        if (args.length < 4) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dmarker set <id> label|icon <value>");
            return;
        }
        String id = args[1];
        String field = args[2].toLowerCase();
        String value = args[3];

        DynmapMarker marker = plugin.getMarkerManager().findMarker(id);
        if (marker == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Marker '" + id + "' not found.");
            return;
        }
        switch (field) {
            case "label" -> { marker.setLabel(value); plugin.getMarkerManager().save(); }
            case "icon"  -> { marker.setIcon(value);  plugin.getMarkerManager().save(); }
            default -> { sender.sendMessage(PREFIX + ChatColor.RED + "Unknown field. Use: label, icon"); return; }
        }
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Marker '" + id + "' updated.");
    }

    private void cmdAddSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dmarker addset <id> [label]");
            return;
        }
        String id = args[1];
        String label = args.length >= 3 ? args[2] : id;
        if (plugin.getMarkerManager().getSet(id) != null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "A marker set with id '" + id + "' already exists.");
            return;
        }
        plugin.getMarkerManager().createSet(id, label, true);
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Marker set '" + label + "' created.");
    }

    private void cmdDeleteSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dmarker deleteset <id>");
            return;
        }
        String id = args[1];
        if (!plugin.getMarkerManager().deleteSet(id)) {
            sender.sendMessage(PREFIX + ChatColor.RED
                    + "Cannot delete set '" + id + "' (not found or protected).");
            return;
        }
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Marker set '" + id + "' deleted.");
    }

    private void cmdListSets(CommandSender sender) {
        if (!sender.hasPermission("smpdynmap.marker")) { noPerms(sender); return; }
        sender.sendMessage(ChatColor.GOLD + "=== Marker Sets ===");
        for (MarkerSet set : plugin.getMarkerManager().getAllSets()) {
            sender.sendMessage(ChatColor.YELLOW + set.getId() + ChatColor.WHITE + " – "
                    + set.getLabel() + ChatColor.GRAY + " (" + set.getMarkers().size() + " markers)");
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void noPerms(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.RED + "You do not have permission to do that.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== DMarker Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker add <id> [label] [icon]" + ChatColor.WHITE + " – add marker at your position");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker delete <id>" + ChatColor.WHITE + " – delete a marker");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker list" + ChatColor.WHITE + " – list all markers");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker set <id> label|icon <value>" + ChatColor.WHITE + " – update a marker");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker addset <id> [label]" + ChatColor.WHITE + " – create a marker set");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker deleteset <id>" + ChatColor.WHITE + " – delete a marker set");
        sender.sendMessage(ChatColor.YELLOW + "/dmarker listsets" + ChatColor.WHITE + " – list all marker sets");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
