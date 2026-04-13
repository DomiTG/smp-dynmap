package com.domitg.smpdynmap.http.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Shared HTTP helper utilities for all handlers.
 */
public final class HttpUtil {

    private HttpUtil() {}

    public static void sendJson(HttpExchange ex, int status, String json,
                                boolean allowCors) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        if (allowCors) {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        }
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().add("Cache-Control", "no-cache");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendBytes(HttpExchange ex, int status, byte[] body,
                                 String contentType, boolean allowCors) throws IOException {
        if (allowCors) {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        }
        ex.getResponseHeaders().add("Content-Type", contentType);
        ex.getResponseHeaders().add("Cache-Control", "public, max-age=30");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }

    /** Handle CORS pre-flight OPTIONS requests. */
    public static boolean handleOptions(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    public static String mimeType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (path.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))  return "image/gif";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if (path.endsWith(".ico"))  return "image/x-icon";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".woff")) return "font/woff";
        return "application/octet-stream";
    }
}
