/*******************************************************************************
 * Copyright 2013 Pekka Hyvönen, pekka@vaadin.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.vaadin.pekka.postmessage;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverClientRpc;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverServerRpc;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverState;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;

/**
 * An extension for listening to Post Messages in a Vaadin application and
 * responding to them. Also helps sending messages to parent window (if any).
 * <p>
 * When using inside an iframe, use {@link #postMessageToParent(String, String)}.
 * <p>
 * When using inside a sub window and want to send to opener window, use
 * {@link #postMessageToOpener(String, String)}.
 * <p>
 * For sending messages to an opened window, use {@link PostMessageWindowExtension}.
 *
 * https://developer.mozilla.org/en-US/docs/DOM/window.postMessage
 *
 * @author pekkahyvonen
 */
@SuppressWarnings("serial")
public class PostMessageReceiverExtension extends AbstractExtension {

    private PostMessageReceiverServerRpc rpc = new PostMessageReceiverServerRpc() {

        @Override
        public void onMessage(int id, String message, String origin,
                boolean cached) {
            fireMessageEvent(message, origin, id, cached);
        }
    };

    public PostMessageReceiverExtension() {
        registerRpc(rpc);
    }

    @Override
    public PostMessageReceiverState getState() {
        return (PostMessageReceiverState) super.getState();
    }

    /**
     * An event fired when a Post Message has been received by
     * {@link PostMessageReceiverExtension} from one of the trusted origins.
     *
     * @author pekkahyvonen
     *
     */
    public static class PostMessageReceiverEvent extends PostMessageEvent {

        private final boolean cached;

        private final PostMessageReceiverExtension extension;

        public PostMessageReceiverEvent(Component source, String origin,
                String message, Integer messageId, boolean cached,
                PostMessageReceiverExtension extension) {
            super(source, origin, message, messageId);
            this.cached = cached;
            this.extension = extension;
        }

        /**
         * Respond to this specific message by sending a Post Message to the
         * source or optionally the parent of the source of this message. The
         * origin and the source of the original message will be automatically
         * reused.
         *
         * If the source of this message was a GWT or a Vaadin application, then
         * the <code>parent</code> should be <code>true</code>, because the
         * message source is actually an iframe running the application instead
         * of the window.
         *
         * @param message
         *            the message to send
         * @param parent
         *            true if the response should be sent to the parent or the
         *            original message's source, false if it should be directly
         *            to the received messages source
         */
        @Override
        public void respond(String message, boolean parent) {
            extension.respondToMessage(getMessageId(), message, parent);
        }

        /**
         * Returns true if this message was received while
         * {@link PostMessageReceiverExtension#isCacheMessages()} mode was on.
         *
         * @return <code>true</code> if message was cached, <code>false</code>
         *         if not
         */
        public boolean isCached() {
            return cached;
        }

    }

    /**
     * Add an accepted message origin. If
     * {@link PostMessageReceiverExtension#isCacheMessages()} mode is on (true),
     * and the cache has any messages for this origin, then those events for
     * this message will fired.
     *
     * @param messageOrigin
     *            the message origin to accept
     */
    public void addAcceptedMessageOrigin(final String messageOrigin) {
        if (messageOrigin == null || messageOrigin.isEmpty()) {
            throw new InvalidParameterException(
                    "Message Origin cannot be null or empty");
        }
        if (!getState().trustedMessageOrigins.contains(messageOrigin)) {
            getState().trustedMessageOrigins.add(messageOrigin);
        }
    }

    /**
     * Remove an accepted message origin. After this, no messages for this
     * origin will get through, but if {@link #isCacheMessages()} mode is on,
     * then they are still cached.
     *
     * @param messageOrigin
     *            the origin to remove
     */
    public void removeAcceptedMessageOrigin(final String messageOrigin) {
        if (messageOrigin == null || messageOrigin.isEmpty()) {
            throw new InvalidParameterException(
                    "Message Origin cannot be null or empty");
        }
        if (getState().trustedMessageOrigins.contains(messageOrigin)) {
            getState().trustedMessageOrigins.remove(messageOrigin);
        }
    }

    /**
     *
     * @return an unmodifiable list of the current accepted message origins
     */
    public List<String> getAcceptedMessageOrigins() {
        return Collections.unmodifiableList(getState().trustedMessageOrigins);
    }

    /**
     * If messages are cached, then those messages that are not from trusted
     * origins will be cached in the client side until the messages' origin will
     * get added to accepted message origins with
     * {@link #addAcceptedMessageOrigin(String)} or until the cache is disabled,
     * which will also clear it.
     *
     * Default value is <code>true</code>.
     *
     * @return true if messages are cached, false if not.
     */
    public boolean isCacheMessages() {
        return getState().cacheMessages;
    }

    /**
     * Set the cache mode on or off. When messages are cached, those messages
     * that are not from trusted origins will be cached in the client side until
     * the messages' origin will get added to accepted message origins with
     * {@link #addAcceptedMessageOrigin(String)}. Disabling the cache will clear
     * all the currently cached messages.
     *
     * Default value is <code>true</code>.
     *
     * @param cacheMessages
     *            true to turn cache on, false to turn it off
     */
    public void setCacheMessages(boolean cacheMessages) {
        getState().cacheMessages = cacheMessages;
    }

    /**
     * Post Message to the parent of the frame (window.parent) that contains
     * this receiver (if there is such parent).
     *
     * If the parent of the window has a different origin, the message will not
     * get posted. For any origin, use "*".
     *
     * @param message
     *            the message to post
     * @param messageOrigin
     *            the expected origin for the parent window
     */
    public void postMessageToParent(final String message,
            final String messageOrigin) {
        if (messageOrigin == null || messageOrigin.isEmpty()) {
            throw new InvalidParameterException(
                    "Message Origin cannot be null or empty");
        }
        getRpcProxy(PostMessageReceiverClientRpc.class).postMessageToParent(
                message, messageOrigin);
    }

    /**
     * Post message to the opener (window.opener) of this window that contains
     * this receiver.
     *
     * If the parent of the window has a different origin, the message will not
     * get posted. For any origin, use "*".
     *
     * @param message
     *            the message to post
     * @param messageOrigin
     *            the target origin
     */
    public void postMessageToOpener(final String message,
            final String messageOrigin) {
        if (messageOrigin == null || messageOrigin.isEmpty()) {
            throw new InvalidParameterException(
                    "Message Origin cannot be null or empty");
        }
        getRpcProxy(PostMessageReceiverClientRpc.class).postMessageToOpener(
                message, messageOrigin);
    }

    protected void fireMessageEvent(final String msg, final String org,
            final Integer id, Boolean cached) {
        fireEvent(new PostMessageReceiverEvent((Component) getParent(), org,
                msg, id, cached, this));
    }

    private static final Method POST_MESSAGE_EVENT_METHOD;

    static {
        try {
            POST_MESSAGE_EVENT_METHOD = PostMessageReceiverListener.class
                    .getDeclaredMethod("onMessage",
                            new Class[] { PostMessageReceiverEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding listener method in PostMessageReceiver");
        }
    }

    /**
     * An interface for listening {@link PostMessageReceiverEvent} events from a
     * {@link PostMessageReceiverExtension}.
     *
     * @author pekkahyvonen
     *
     */
    public interface PostMessageReceiverListener extends Serializable {

        /**
         * Called on a {@link PostMessageReceiverEvent} from a
         * {@link PostMessageReceiverExtension} represented by
         * {@link PostMessageReceiverEvent#getSource()};
         *
         * @param event
         *            the message event
         */
        public void onMessage(PostMessageReceiverEvent event);
    }

    /**
     * Registers a {@link PostMessageReceiverListener} listener for
     * {@link PostMessageReceiverEvent} events from this
     * {@link PostMessageReceiverExtension}.
     *
     * @param listener
     *            the listener to register
     */
    public void addListener(PostMessageReceiverListener listener) {
        addPostMessageListener(listener);
    }

    /**
     * Registers a {@link PostMessageReceiverListener} listener for
     * {@link PostMessageReceiverEvent} events from this
     * {@link PostMessageReceiverExtension}.
     *
     * @param listener
     *            the listener to register
     */
    public void addPostMessageListener(PostMessageReceiverListener listener) {
        addListener(PostMessageReceiverEvent.class, listener,
                POST_MESSAGE_EVENT_METHOD);
    }

    /**
     * Removes a registered {@link PostMessageReceiverListener} listener for
     * {@link PostMessageReceiverEvent} events from this
     * {@link PostMessageReceiverExtension}.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeListener(PostMessageReceiverListener listener) {
        removePostMessageListener(listener);
    }

    /**
     * Removes a registered {@link PostMessageReceiverListener} listener for
     * {@link PostMessageReceiverEvent} events from this
     * {@link PostMessageReceiverExtension}.
     *
     * @param listener
     *            the listener to remove
     */
    public void removePostMessageListener(PostMessageReceiverListener listener) {
        removeListener(PostMessageReceiverEvent.class, listener,
                POST_MESSAGE_EVENT_METHOD);
    }

    protected void respondToMessage(Integer messageId, String message,
            boolean parent) {
        getRpcProxy(PostMessageReceiverClientRpc.class).respondToMessage(
                messageId, message, parent);
    }

}
