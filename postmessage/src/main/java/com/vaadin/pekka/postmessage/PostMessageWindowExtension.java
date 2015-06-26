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

import com.vaadin.pekka.postmessage.client.postmessagewindowutil.PostMessageWindowUtilClientRpc;
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
public class PostMessageWindowExtension extends PostMessageReceiverExtension {

    /**
     *
     */
    public class PostMessageWindow {
        final int id;

        public PostMessageWindow(int id) {
            this.id = id;
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
    }

    private int counter = 0;

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
     * @return
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
        getRpcProxy(PostMessageWindowUtilClientRpc.class).openWindow(url,
                origin, features);
        return new PostMessageWindow(id);
    }

    protected void postMessageToWindow(int id, String message) {
        getRpcProxy(PostMessageWindowUtilClientRpc.class).postMessage(message,
                id);
    }
}
