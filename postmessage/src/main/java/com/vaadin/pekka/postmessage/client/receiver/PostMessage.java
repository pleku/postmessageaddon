package com.vaadin.pekka.postmessage.client.receiver;

import com.google.gwt.core.client.JavaScriptObject;

public class PostMessage {
    final int id;
    final String origin;
    final String message;
    final JavaScriptObject source;
    final boolean cached;

    public PostMessage(int id, String message, String origin,
            JavaScriptObject source, boolean cached) {
        this.id = id;
        this.message = message;
        this.origin = origin;
        this.source = source;
        this.cached = cached;
    }

    @Override
    public String toString() {
        return "PostMessage [id=" + id + ", origin=" + origin + ", message="
                + message + ", source=" + source + ", cached=" + cached + "]";
    }

}