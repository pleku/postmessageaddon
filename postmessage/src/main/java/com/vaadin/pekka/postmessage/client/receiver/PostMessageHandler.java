package com.vaadin.pekka.postmessage.client.receiver;

import com.google.gwt.event.shared.EventHandler;

public interface PostMessageHandler extends EventHandler {

    public void onMessage(PostMessageEvent message);
}
