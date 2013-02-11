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

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.pekka.postmessage.PostMessageReceiver;
import com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverWidget.Message;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostMessageReceiver.class)
public class PostMessageReceiverConnector extends AbstractComponentConnector {

    PostMessageReceiverServerRpc rpc = RpcProxy.create(
            PostMessageReceiverServerRpc.class, this);

    @Override
    protected void init() {
        super.init();
        registerRpc(PostMessageReceiverClientRpc.class,
                new PostMessageReceiverClientRpc() {
                    public void postMessageToParent(String message,
                            String origin) {
                        getWidget().postMessageToParent(message, origin);
                    }

                    @Override
                    public void respondToMessage(int id, String message,
                            boolean parent) {
                        getWidget().respondToMessage(id, message, parent);
                    }
                });

        getWidget().addPostMessageReceiverHandler(
                new PostMessageReceiverWidget.PostMessageReceiverHandler() {

                    @Override
                    public void onMessage(Message message) {
                        rpc.onMessage(message.id, message.message,
                                message.origin, message.cached);
                    }
                });
    }

    @Override
    public PostMessageReceiverWidget getWidget() {
        return (PostMessageReceiverWidget) super.getWidget();
    }

    @Override
    public PostMessageReceiverState getState() {
        return (PostMessageReceiverState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (getState().cacheMessages != getWidget().cacheMessages) {
            getWidget().setCacheMessages(getState().cacheMessages);
        }

        if (stateChangeEvent.getChangedProperties().contains(
                "trustedMessageOrigins")) {
            getWidget().setupMessageOrigins(getState().trustedMessageOrigins);
        }
    }

}
