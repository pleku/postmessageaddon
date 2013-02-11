package com.vaadin.pekka.postmessage.client.postmessagewindowutil;

import com.vaadin.shared.communication.ClientRpc;

public interface PostMessageWindowUtilClientRpc extends ClientRpc {

    public void openWindow(String url, String origin, String features);

    public void postMessage(String message, int messageWindowId);

    public void closeWindow(int messageWindowId);

}