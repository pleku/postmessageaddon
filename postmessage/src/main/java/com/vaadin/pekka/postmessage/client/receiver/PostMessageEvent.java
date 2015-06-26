package com.vaadin.pekka.postmessage.client.receiver;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.GwtEvent;

public class PostMessageEvent extends GwtEvent<PostMessageHandler> {

    private static Type<PostMessageHandler> TYPE;

    private final PostMessage message;

    public PostMessageEvent(PostMessage message) {
        this.message = message;
    }

    /**
     * Returns the post message for this event.
     *
     * @return the message
     */
    public PostMessage getMessage() {
        return message;
    }

    /**
     * Fires a value change event on all registered handlers in the handler
     * manager. If no such handlers exist, this method will do nothing.
     *
     * @param <T>
     *            the old value type
     * @param source
     *            the source of the handlers
     * @param message
     *            the post message
     */
    public static <T> void fire(HasValueChangeHandlers<T> source,
            PostMessage message) {
        if (TYPE != null) {
            PostMessageEvent event = new PostMessageEvent(message);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<PostMessageHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PostMessageHandler>();
        }
        return TYPE;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<PostMessageHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    public String toDebugString() {
        return super.toDebugString() + getMessage().toString();
    }

    @Override
    protected void dispatch(PostMessageHandler handler) {
        handler.onMessage(this);
    }

}
