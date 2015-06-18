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

import com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameClientRpc;
import com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameServerRpc;
import com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameState;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;

/**
 * A component for opening a different origin location in an iframe for sending
 * & receiving Post Messages from it.
 * 
 * https://developer.mozilla.org/en-US/docs/DOM/window.postMessage
 * 
 * @author pekkahyvonen
 * 
 */
@SuppressWarnings("serial")
public class PostMessageIFrame extends BrowserFrame {

    private PostMessageIFrameServerRpc rpc = new PostMessageIFrameServerRpc() {

        @Override
        public void onMessage(String message, String origin) {
            fireMessageEvent(message, origin, -1);
        }
    };

    /**
     * An event for a Post Message received from a {@link PostMessageIFrame}
     * component.
     * 
     * @author pekkahyvonen
     * 
     */
    public static class PostMessageIFrameEvent extends PostMessageEvent {

        public PostMessageIFrameEvent(Component source, String origin,
                String message, Integer messageId) {
            super(source, origin, message, messageId);
        }

        /**
         * Respond to this specific message by sending a Post Message to same
         * {@link PostMessageIFrame} that this {@link PostMessageIFrameEvent}
         * originated from.
         * 
         * For more info, see
         * {@link PostMessageIFrame#postMessageToIFrame(String, boolean)}
         * 
         * @param message
         * @param listenToResponse
         *            true if need to listen for response, false if no need to
         *            listen for response
         */
        @Override
        public void respond(String message, boolean listenToResponse) {
            ((PostMessageIFrame) super.getComponent()).postMessageToIFrame(
                    message, listenToResponse);
        }

    }

    public PostMessageIFrame() {
        this(null, null, null);
    }

    /**
     * Creates an empty iframe with caption.
     * 
     * @param caption
     */
    public PostMessageIFrame(String caption) {
        this(caption, null, null);
    }

    /**
     * Creates an iframe with the given location and origin.
     * 
     * @param sourceURL
     *            the Source of the iframe.
     * @param iframeOrigin
     *            the origin for the iframe
     */
    public PostMessageIFrame(String sourceURL, String iframeOrigin) {
        this(null, sourceURL, iframeOrigin);
    }

    /**
     * Creates an iframe with the given caption, location and origin.
     * 
     * @param caption
     * @param sourceURL
     *            the Source of the iframe
     * @param iframeOrigin
     *            the origin for the iframe
     */
    public PostMessageIFrame(String caption, String sourceURL,
            String iframeOrigin) {
        super(caption, sourceURL == null || sourceURL.isEmpty() ? null
                : new ExternalResource(sourceURL));
        registerRpc(rpc);
        getState().iframeOrigin = iframeOrigin;
    }

    @Override
    public PostMessageIFrameState getState() {
        return (PostMessageIFrameState) super.getState();
    }

    /**
     * Post a message to the iframe. If the {@link #isAlwaysListen()}==true,
     * then the listenToResponse parameter is ignored. Otherwise, on client side
     * a postMessage listener will be added and removed right after the next
     * received message (without actually knowing if it was the actual response
     * to this message").
     * 
     * @param message
     *            the message to send
     * @param listenToResponse
     *            true if add a listener for the next message only, false is not
     */
    public void postMessageToIFrame(final String message,
            boolean listenToResponse) {
        if (getIframeOrigin() == null || getIframeOrigin().isEmpty()) {
            throw new InvalidParameterException(
                    "Iframe Origin cannot be null or empty, set a value to it before trying to post a message to iframe.");
        }
        if (message != null && !message.isEmpty()) {
            getRpcProxy(PostMessageIFrameClientRpc.class).postMessage(message,
                    listenToResponse);
        } else {
            throw new InvalidParameterException(
                    "Message and Target Origin cannot be null or empty");
        }
    }

    /**
     * 
     * @return the current iframeOrigin used for posting and receiving messages
     */
    public String getIframeOrigin() {
        return getState().iframeOrigin;
    }

    /**
     * Set the iframe origin that will be used for messaging. If the iframe's
     * actual origin will be something else than this, the messages won't be
     * send and any possible received messages will be ignored.
     * 
     * @param iframeOrigin
     */
    public void setIframeOrigin(String iframeOrigin) {
        if (iframeOrigin == null || iframeOrigin.isEmpty()) {
            throw new InvalidParameterException(
                    "Iframe Origin cannot be null or empty, set a value to it before trying to post a message to iframe.");
        }
        getState().iframeOrigin = iframeOrigin;
    }

    /**
     * Default value is false
     * 
     * @return true if always listening for messages from the iframe's origin,
     *         false if not
     */
    public boolean isAlwaysListen() {
        return getState().alwaysListen;
    }

    /**
     * Start or stop to listen to messages from the iframe's origin.
     * 
     * @param alwaysListen
     */
    public void setAlwaysListen(boolean alwaysListen) {
        getState().alwaysListen = alwaysListen;
    }

    protected void fireMessageEvent(final String msg, final String org,
            final Integer id) {
        fireEvent(new PostMessageIFrameEvent(this, org, msg, id));
    }

    private static final Method POST_MESSAGE_EVENT_METHOD;

    static {
        try {
            POST_MESSAGE_EVENT_METHOD = PostMessageIFrameListener.class
                    .getDeclaredMethod("onMessage",
                            new Class[] { PostMessageIFrameEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding listener method in PostMessageReceiver");
        }
    }

    /**
     * An interface for listening {@link PostMessageIFrameEvent} events from a
     * {@link PostMessageIFrame}.
     * 
     * @author pekkahyvonen
     * 
     */
    public interface PostMessageIFrameListener extends Serializable {

        /**
         * Called on a {@link PostMessageIFrameEvent} from a
         * {@link PostMessageIFrame} represented by
         * {@link PostMessageIFrameEvent#getSource()}.
         * 
         * @param event
         */
        public void onMessage(PostMessageIFrameEvent event);
    }

    /**
     * Registers a {@link PostMessageIFrameListener} listener for
     * {@link PostMessageIFrameEvent} events from this {@link PostMessageIFrame}
     * .
     * 
     * @param listener
     *            the listener to register
     */
    public void addListener(PostMessageIFrameListener listener) {
        addPostMessageListener(listener);
    }

    /**
     * Registers a {@link PostMessageIFrameListener} listener for
     * {@link PostMessageIFrameEvent} events from this {@link PostMessageIFrame}
     * .
     * 
     * @param listener
     *            the listener to register
     */
    public void addPostMessageListener(PostMessageIFrameListener listener) {
        addListener(PostMessageIFrameEvent.class, listener,
                POST_MESSAGE_EVENT_METHOD);
    }

    /**
     * Removes a registered {@link PostMessageIFrameListener} listener for
     * {@link PostMessageIFrameEvent} events from this {@link PostMessageIFrame}
     * .
     * 
     * @param listener
     *            the listener to remove
     */
    public void removeListener(PostMessageIFrameListener listener) {
        removePostMessageListener(listener);
    }

    /**
     * Removes a registered {@link PostMessageIFrameListener} listener for
     * {@link PostMessageIFrameEvent} events from this {@link PostMessageIFrame}
     * .
     * 
     * @param listener
     *            the listener to remove
     */
    public void removePostMessageListener(PostMessageIFrameListener listener) {
        removeListener(PostMessageIFrameEvent.class, listener,
                POST_MESSAGE_EVENT_METHOD);
    }
}
