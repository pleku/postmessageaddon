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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageListener;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageReceiverEvent;
import com.vaadin.pekka.postmessage.client.postmessagewindow.PostMessageWindowClientRpc;
import com.vaadin.pekka.postmessage.client.postmessagewindow.PostMessageWindowServerRpc;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * An extension that enables opening of browser windows/tabs and sending
 * PostMessages into them.
 * https://developer.mozilla.org/en-US/docs/DOM/window.postMessage
 *
 * @author pekka@vaadin.com
 *
 */
@SuppressWarnings("serial")
public class PostMessageWindowExtension extends AbstractExtension {

    private PostMessageWindowServerRpc rpc = new PostMessageWindowServerRpc() {

        @Override
        public void onMessage(String message, String origin, final int id) {
            for (PostMessageWindow window : windows) {
                if (window.getId() == id) {
                    // FIXME hacky, should use separate event

                    PostMessageReceiverEvent event = new PostMessageReceiverEvent(
                            getUI(), origin, message, window.messageId++,
                            false, null) {

                        @Override
                        public void respond(String message, boolean parent) {
                            postMessageToWindow(id, message);
                        };
                    };
                    for (PostMessageListener listener : window.listeners) {
                        listener.onMessage(event);
                    }
                    break;
                }
            }
        }
    };

    private int counter = 0;

    private Set<PostMessageWindow> windows = new HashSet<PostMessageWindow>();

    /**
     * Class representing an opened popup window. Use this to send and receive
     * messages to to/from this window only.
     */
    public class PostMessageWindow {

        private final int id;

        private int messageId = 0;

        private List<PostMessageListener> listeners;

        protected PostMessageWindow(int id) {
            this.id = id;
            registerRpc(rpc);
        }

        protected PostMessageWindow(int id, PostMessageListener listener) {
            this(id);
            addPostMessageListener(listener);
        }

        /**
         * Post a message to this window.
         *
         * @param message
         *            the message to post
         */
        public void postMessage(String message) {
            postMessageToWindow(id, message);
        }

        /**
         * Returns the id, a running number, of this popup window.
         *
         * @return the id of the window
         */
        public int getId() {
            return id;
        }

        /**
         * Add a listener to this window for receiving post messages.
         *
         * @param listener
         *            the listener to add
         */
        public void addPostMessageListener(PostMessageListener listener) {
            if (listeners == null) {
                listeners = new ArrayList<PostMessageListener>();
            }
            listeners.add(listener);
        }

        /**
         * Removes the listener from this window.
         *
         * @param listener
         *            the listener to remove.
         */
        public void removePostMessageListener(PostMessageListener listener) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public PostMessageWindowExtension() {
        super();
    }

    /**
     * Creates a new extension and attaches (adds it as an extension) it to the
     * given UI.
     * <p>
     * To detach, call {@link #remove()}.
     *
     * @param ui
     */
    public PostMessageWindowExtension(UI ui) {
        super();
        extend(ui);
    }

    /**
     * Enables opening new windows for Post Messaging from the given UI.
     * <p>
     * To detach, call {@link #remove()}.
     *
     * @param target
     *            the UI to attach the extension to
     */
    public void extend(UI target) {
        super.extend(target);
    }

    /**
     * Opens a window with the given location and features, and uses the given
     * origin for enabling Post Messaging to the window content. The Window name
     * will be '_blank'.
     *
     * Returns an object {@link PostMessageWindow} that can be used for sending
     * Post Messages for the opened window.
     *
     * @param url
     *            the window location to open
     * @param origin
     *            the origin to use for Post Messaging, e.g. for
     *            http://origin.com/app it is http://origin.com
     * @param features
     *            a comma separated list of features, as described i.e.
     *            http://www.w3schools.com/jsref/met_win_open.asp
     * @return the window object for receiving and sending messages to/from this
     *         window
     */
    public PostMessageWindow openWindow(String url, String origin,
            String features) {
        final int id = counter++;
        if (url == null || url.isEmpty()) {
            throw new InvalidParameterException(
                    "Window url cannot be null or empty");
        }
        if (origin == null || origin.isEmpty()) {
            throw new InvalidParameterException(
                    "Window origin cannot be null or empty");
        }
        getRpcProxy(PostMessageWindowClientRpc.class).openWindow(url, id,
                origin, features);
        PostMessageWindow postMessageWindow = new PostMessageWindow(id);
        windows.add(postMessageWindow);
        return postMessageWindow;
    }

    protected void postMessageToWindow(int id, String message) {
        getRpcProxy(PostMessageWindowClientRpc.class).postMessage(message, id);
    }
}
