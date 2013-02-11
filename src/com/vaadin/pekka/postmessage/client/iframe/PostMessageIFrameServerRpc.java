package com.vaadin.pekka.postmessage.client.iframe;

import com.vaadin.shared.communication.ServerRpc;

public interface PostMessageIFrameServerRpc extends ServerRpc {

    public void onMessage(String message, String origin);

}
