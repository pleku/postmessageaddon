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
package com.vaadin.pekka.postmessage.client.receiver;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostMessageReceiverExtension.class)
public class PostMessageReceiverConnector extends AbstractExtensionConnector {

    PostMessageReceiverServerRpc rpc = RpcProxy.create(
            PostMessageReceiverServerRpc.class, this);

    private PostMessageReceiver receiver;

    @Override
    protected void init() {
        super.init();
        receiver = new PostMessageReceiver();
        receiver.addPostMessageHandler(new PostMessageHandler() {

            @Override
            public void onMessage(PostMessageEvent message) {
                rpc.onMessage(message.getMessage().id,
                        message.getMessage().message,
                        message.getMessage().origin,
                        message.getMessage().cached);
            }
        });
        registerRpc(PostMessageReceiverClientRpc.class,
                new PostMessageReceiverClientRpc() {
                    public void postMessageToParent(String message,
                            String origin) {
                        receiver.postMessageToParent(message, origin);
                    }

                    @Override
                    public void respondToMessage(int id, String message,
                            boolean parent) {
                        receiver.respondToMessage(id, message, parent);
                    }

                    @Override
                    public void postMessageToOpener(String message,
                            String origin) {
                        receiver.postMessageToOpener(message, origin);
                    }
                });

    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        receiver.remove();
    }

    protected native void pushRPCupdate(ApplicationConnection conn)
    /*-{
        conn.@com.vaadin.client.ApplicationConnection::doSendPendingVariableChanges()();
    }-*/;

    @Override
    public PostMessageReceiverState getState() {
        return (PostMessageReceiverState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (getState().cacheMessages != receiver.cacheMessages) {
            receiver.setCacheMessages(getState().cacheMessages);
        }

        if (stateChangeEvent.hasPropertyChanged("trustedMessageOrigins")) {
            receiver.setupMessageOrigins(getState().trustedMessageOrigins);
        }
    }

    @Override
    protected void extend(ServerConnector target) {
        // NOOP
    }

}
