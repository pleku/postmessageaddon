package com.vaadin.pekka.postmessage.client.receiver;

import com.vaadin.shared.communication.ServerRpc;

public interface PostMessageReceiverServerRpc extends ServerRpc {

	public void onMessage(int id, String message, String origin, boolean cached);

}
