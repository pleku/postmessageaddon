package com.vaadin.pekka.postmessage.client.iframe;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.browserframe.BrowserFrameConnector;
import com.vaadin.pekka.postmessage.PostMessageIFrame;
import com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameWidget.PostMessageIFrameWidgetHandler;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostMessageIFrame.class)
public class PostMessageIFrameConnector extends BrowserFrameConnector {

    @Override
    protected void init() {
        super.init();
        registerRpc(PostMessageIFrameClientRpc.class,
                new PostMessageIFrameClientRpc() {
                    public void postMessage(String message,
                            boolean listenToResponse) {
                        getWidget().postMessageToIFrame(message,
                                listenToResponse);
                    }
                });

        getWidget().addHandler(new PostMessageIFrameWidgetHandler() {

            @Override
            public void onMessage(String message, String origin) {
                getRpcProxy(PostMessageIFrameServerRpc.class).onMessage(
                        message, origin);
            }
        });
    }

    @Override
    public PostMessageIFrameWidget getWidget() {
        return (PostMessageIFrameWidget) super.getWidget();
    }

    @Override
    public PostMessageIFrameState getState() {
        return (PostMessageIFrameState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        super.onStateChanged(event);

        if (getState().iframeOrigin != null
                && !getState().iframeOrigin.equals(getWidget().iframeOrigin)) {
            getWidget().iframeOrigin = getState().iframeOrigin;
        }

        if (getState().alwaysListen != getWidget().alwaysListen) {
            getWidget().alwaysListen = getState().alwaysListen;
            getWidget().pendingResponses = 0;
            getWidget().setupListener();
        }

    }

}
