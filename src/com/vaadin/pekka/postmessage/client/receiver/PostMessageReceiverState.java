package com.vaadin.pekka.postmessage.client.receiver;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class PostMessageReceiverState extends com.vaadin.shared.AbstractComponentState {

	public boolean cacheMessages = true;
	
	public ArrayList<String> trustedMessageOrigins = new ArrayList<String>();
}