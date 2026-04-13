package com.domitg.smpdynmap.model;

/**
 * A single chat message recorded for the web update feed.
 */
public class ChatMessage {

    private final String playerName;
    private final String message;
    private final String channel;
    private final long timestamp;

    public ChatMessage(String playerName, String message, String channel) {
        this.playerName = playerName;
        this.message = message;
        this.channel = channel;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public String getMessage() { return message; }
    public String getChannel() { return channel; }
    public long getTimestamp() { return timestamp; }
}
