package com.vaadin.pekka.postmessage.client.receiver;

import com.vaadin.shared.communication.ClientRpc;

public interface PostMessageReceiverClientRpc extends ClientRpc {

	public void postMessageToParent(String message, String origin);
	
	public void respondToMessage(int id, String message, boolean parent);

}