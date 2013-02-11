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
package com.vaadin.pekka.postmessage.client.postmessagewindowutil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.pekka.postmessage.PostMessageWindowUtil;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostMessageWindowUtil.class)
public class PostMessageWindowUtilConnector extends AbstractExtensionConnector {

    PostMessageWindowUtilServerRpc rpc = RpcProxy.create(
            PostMessageWindowUtilServerRpc.class, this);

    private JsArrayString origins = JsArrayString.createArray().cast();
    private JsArray<JavaScriptObject> windows = JsArray.createArray().cast();

    public PostMessageWindowUtilConnector() {
        registerRpc(PostMessageWindowUtilClientRpc.class,
                new PostMessageWindowUtilClientRpc() {

                    @Override
                    public void openWindow(String url, String origin,
                            String features) {
                        windows.push(PostMessageWindowUtilConnector.this
                                .openWindow(url, features));
                        origins.push(origin);
                    }

                    @Override
                    public void postMessage(String message, int messageWindowId) {
                        postMessageToWindow(windows.get(messageWindowId),
                                message, origins.get(messageWindowId));
                    }

                    @Override
                    public void closeWindow(int messageWindowId) {
                        PostMessageWindowUtilConnector.this.closeWindow(windows
                                .get(messageWindowId));
                    }
                });

    }

    @Override
    protected void extend(ServerConnector target) {
        // nothing to do here
    }

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
