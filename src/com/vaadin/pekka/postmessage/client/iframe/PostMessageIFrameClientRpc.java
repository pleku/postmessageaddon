package com.vaadin.pekka.postmessage.client.iframe;

import com.vaadin.shared.communication.ClientRpc;

public interface PostMessageIFrameClientRpc extends ClientRpc {

    public void postMessage(String message, boolean listenToResponse);

}