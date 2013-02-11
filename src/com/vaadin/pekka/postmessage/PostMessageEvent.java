package com.vaadin.pekka.postmessage;

import com.vaadin.ui.Component;

/**
 * Represents the basic properties of a Post Message event.
 * 
 * @author pekkahyvonen
 * 
 */
@SuppressWarnings("serial")
public abstract class PostMessageEvent extends Component.Event {

    private final String origin;
    private final String message;
    private final Integer messageId;

    public PostMessageEvent(final Component source, final String origin,
            final String message, final Integer messageId) {
        super(source);
        this.origin = origin;
        this.message = message;
        this.messageId = messageId;
    }

    /**
     * 
     * @return the origin where the message came from
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * 
     * @return the actual message
     */
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @return an id for the message, or -1 if component doesn't identify
     *         messages with ids
     */
    public Integer getMessageId() {
        return messageId;
    }

    // for responding to the specific message this event represents
    public abstract void respond(final String message, final boolean option);

}
