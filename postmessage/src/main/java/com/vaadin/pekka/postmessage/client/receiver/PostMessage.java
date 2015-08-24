package com.vaadin.pekka.postmessage.client.receiver;

import com.google.gwt.core.client.JavaScriptObject;

public class PostMessage {
    public final int id;
    public final String origin;
    public final String message;
    public final JavaScriptObject source;
    public final boolean cached;

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
                + message + ", cached=" + cached + "]";
    }

}