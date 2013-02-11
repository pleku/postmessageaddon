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
