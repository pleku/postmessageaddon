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
package com.vaadin.pekka.postmessage.client.iframe;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.browserframe.BrowserFrameConnector;
import com.vaadin.pekka.postmessage.PostMessageIFrame;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageEvent;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageHandler;
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

        getWidget().addPostMessageHandler(new PostMessageHandler() {

            @Override
            public void onMessage(PostMessageEvent message) {
                getRpcProxy(PostMessageIFrameServerRpc.class).onMessage(
                        message.getMessage().message,
                        message.getMessage().origin, message.getMessage().id);
                pushRPCupdate(getConnection());
            }
        });
    }

    protected native void pushRPCupdate(ApplicationConnection conn)
    /*-{
        conn.@com.vaadin.client.ApplicationConnection::doSendPendingVariableChanges()();
    }-*/;

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
