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
package com.vaadin.pekka.postmessage.client.postmessagewindow;

import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.pekka.postmessage.PostMessageWindowExtension;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageEvent;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageHandler;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiver;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostMessageWindowExtension.class)
public class PostMessageWindowConnector extends AbstractExtensionConnector {

    PostMessageWindowServerRpc rpc = RpcProxy.create(
            PostMessageWindowServerRpc.class, this);

    private JsArrayString origins = JsArrayString.createArray().cast();
    private JsArray<JavaScriptObject> windows = JsArray.createArray().cast();

    private PostMessageReceiver receiver;

    private Logger logger = Logger.getLogger(PostMessageWindowConnector.class
            .getName());

    public PostMessageWindowConnector() {
        registerRpc(PostMessageWindowClientRpc.class,
                new PostMessageWindowClientRpc() {

                    @Override
                    public void openWindow(String url, int messageWindowId,
                            String origin, String features) {
                        windows.push(PostMessageWindowConnector.this
                                .openWindow(url, features));
                        origins.push(origin);
                        receiver.addMessageOrigin(origin);
                    }

                    @Override
                    public void postMessage(String message, int messageWindowId) {
                        postMessageToWindow(windows.get(messageWindowId),
                                message, origins.get(messageWindowId));
                    }

                    @Override
                    public void closeWindow(int messageWindowId) {
                        PostMessageWindowConnector.this.closeWindow(windows
                                .get(messageWindowId));
                    }
                });

        receiver = new PostMessageReceiver();
        receiver.addPostMessageHandler(new PostMessageHandler() {

            @Override
            public void onMessage(PostMessageEvent message) {
                final JavaScriptObject source = message.getMessage().source;
                for (int i = 0; i < windows.length(); i++) {
                    final JavaScriptObject window = windows.get(i);
                    if (checkWindowEquality(source, window)) {
                        rpc.onMessage(message.getMessage().message,
                                message.getMessage().origin, i);
                        return;
                    }
                }
                logger.severe("Received message from unknown source: "
                        + message.getMessage().toString());
            }
        });
    }

    @Override
    protected void extend(ServerConnector target) {
        // nothing to do here
    }

    protected native boolean checkWindowEquality(JavaScriptObject source,
            JavaScriptObject window)
    /*-{
        return source == window || (source.parent && source.parent == window);
    }-*/;

    protected native JavaScriptObject openWindow(String url, String features)
    /*-{
        var w = $wnd.open('','_blank',features);
        w.location.assign(url);
        w.focus();
        return w;
    }-*/;

    protected native void postMessageToWindow(JavaScriptObject w,
            String message, String origin)
    /*-{
        w.postMessage(message, origin);
     }-*/;

    protected native void closeWindow(JavaScriptObject w)
    /*-{
        w.open('', '_self', '');
        w.close();
    }-*/;

}
