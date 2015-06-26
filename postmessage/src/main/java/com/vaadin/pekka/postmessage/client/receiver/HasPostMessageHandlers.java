package com.vaadin.pekka.postmessage.client.receiver;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasPostMessageHandlers extends HasHandlers {

    public HandlerRegistration addPostMessageHandler(PostMessageHandler handler);
}
