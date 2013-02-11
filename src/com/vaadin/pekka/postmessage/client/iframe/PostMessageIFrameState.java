package com.vaadin.pekka.postmessage.client.iframe;

import com.vaadin.shared.ui.browserframe.BrowserFrameState;

@SuppressWarnings("serial")
public class PostMessageIFrameState extends BrowserFrameState {

    public boolean alwaysListen = false;
    public String iframeOrigin;

}