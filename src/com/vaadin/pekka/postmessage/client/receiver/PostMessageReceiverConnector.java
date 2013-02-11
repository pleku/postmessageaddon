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
