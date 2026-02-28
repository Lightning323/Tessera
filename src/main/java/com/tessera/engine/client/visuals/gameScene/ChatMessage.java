package com.tessera.engine.client.visuals.gameScene;

class ChatMessage {
    public ChatMessage(String value) {
        this.value = value;
        time = System.currentTimeMillis();
    }

    public String value;
    public long time;
}
