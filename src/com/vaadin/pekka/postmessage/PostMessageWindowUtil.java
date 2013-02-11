package com.vaadin.pekka.postmessage;

import java.security.InvalidParameterException;

import com.vaadin.pekka.postmessage.client.postmessagewindowutil.PostMessageWindowUtilClientRpc;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * An extension that enables opening of browser windows/tabs and sending
 * PostMessages into them.
 * https://developer.mozilla.org/en-US/docs/DOM/window.postMessage
 * 
 * TODO add a way to listen to messages from a specific opened window
 * 
 * @author pekka@vaadin.com
 * 
 */
@SuppressWarnings("serial")
public class PostMessageWindowUtil extends AbstractExtension {

    public class PostMessageWindow {
        final int id;

        public PostMessageWindow(int id) {
            this.id = id;
        }

        public void postMessage(String message) {
            postMessageToWindow(id, message);
        }
    }

    // private PostMessageWindowUtilServerRpc rpc = new
    // PostMessageWindowUtilServerRpc() {
    // TODO
    // };

    private int counter = 0;

    public PostMessageWindowUtil() {
        // registerRpc(rpc);
    }

    /**
     * Enables opening new windows for Post Messaging from the given UI.
     * 
     * @param target
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
     *            the origin to use for Post Messaging
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
