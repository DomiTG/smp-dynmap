package com.domitg.smpdynmap.http.handlers;

import com.domitg.smpdynmap.SmpDynmap;
import com.domitg.smpdynmap.model.ChatMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Accepts a web-chat message and broadcasts it in-game.
 *
 * <pre>
 * POST /up/sendchat
 * Content-Type: application/json
 * { "name": "WebUser", "message": "Hello from the web!" }
 * </pre>
 */
public class SendChatHandler implements HttpHandler {

    private final SmpDynmap plugin;
    private final boolean allowCors;

    public SendChatHandler(SmpDynmap plugin) {
        this.plugin = plugin;
        this.allowCors = plugin.getDynmapConfig().isAllowCors();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (HttpUtil.handleOptions(ex)) return;

        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendError(ex, 405, "Method Not Allowed");
            return;
        }

        if (!plugin.getDynmapConfig().isChatEnabled()) {
            HttpUtil.sendJson(ex, 403, "{\"error\":\"Web chat is disabled\"}", allowCors);
            return;
        }

        String name;
        String message;
        try (InputStreamReader reader = new InputStreamReader(
                ex.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject body = JsonParser.parseReader(reader).getAsJsonObject();
            name    = body.has("name")    ? body.get("name").getAsString()    : "WebUser";
            message = body.has("message") ? body.get("message").getAsString() : "";
        } catch (Exception e) {
            HttpUtil.sendError(ex, 400, "Bad Request: invalid JSON");
            return;
        }

        // Sanitize
        name    = ChatColor.stripColor(name).replaceAll("[^a-zA-Z0-9_\\- ]", "").trim();
        message = ChatColor.stripColor(message).trim();

        if (name.isEmpty()) name = "WebUser";
        if (message.isEmpty()) {
            HttpUtil.sendJson(ex, 400, "{\"error\":\"Empty message\"}", allowCors);
            return;
        }

        // Limit lengths
        name    = name.substring(0, Math.min(name.length(), 32));
        message = message.substring(0, Math.min(message.length(), 256));

        final String finalName = name;
        final String finalMsg  = message;

        // Broadcast must happen on the main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            String broadcast = ChatColor.GRAY + "[Web] " + ChatColor.WHITE
                    + finalName + ChatColor.GRAY + ": " + ChatColor.WHITE + finalMsg;
            plugin.getServer().broadcastMessage(broadcast);
        });

        // Record in chat history
        plugin.getUpdateManager().addChatMessage(
                new ChatMessage("[Web] " + name, message, "web"));

        HttpUtil.sendJson(ex, 200, "{\"result\":\"success\"}", allowCors);
    }
}
