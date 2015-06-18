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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;

public class PostMessageReceiverWidget extends Widget {

    public interface PostMessageReceiverHandler {
        public void onMessage(Message message);
    }

    protected class Message {
        int id;
        String origin;
        String message;
        JavaScriptObject source;
        boolean cached;

        public Message(int id, String message, String origin,
                JavaScriptObject source, boolean cached) {
            this.id = id;
            this.message = message;
            this.origin = origin;
            this.source = source;
            this.cached = cached;
        }
    }

    public static final String CLASSNAME = "postmessage-receiver";

    protected final List<String> trustedMessageOrigins;

    protected final List<Message> cachedMessages;

    protected List<Message> messageList;

    protected int messageCounter = 0;

    protected final List<PostMessageReceiverHandler> handlers;

    protected final boolean isIE8;

    boolean cacheMessages = true;

    public PostMessageReceiverWidget() {
        createDOM();

        trustedMessageOrigins = new ArrayList<String>();
        cachedMessages = new ArrayList<Message>();
        messageList = new ArrayList<Message>();
        handlers = new ArrayList<PostMessageReceiverHandler>();
        listener = addMessageHandler();
        isIE8 = BrowserInfo.get().isIE8();
    }

    protected void createDOM() {
        setElement(Document.get().createDivElement());
        setStyleName(CLASSNAME);
        getStyleElement().getStyle().setDisplay(Display.NONE);
    }

    public void addPostMessageReceiverHandler(PostMessageReceiverHandler handler) {
        handlers.add(handler);
    }

    public void removePostMessageReceiverHandler(
            PostMessageReceiverHandler handler) {
        handlers.remove(handler);
    }

    public void respondToMessage(int msgId, String msg, boolean parent) {
        for (Message m : messageList) {
            if (m.id == msgId) {
                respondToMessage(m.source, parent, msg, m.origin);
            }
        }
    }

    public void setCacheMessages(boolean cache) {
        cacheMessages = cache;
        if (cacheMessages) {
            if (listener == null) {
                listener = addMessageHandler();
            }
        } else {
            cachedMessages.clear();
            if (trustedMessageOrigins.isEmpty()) {
                removeMessageHandler(listener);
                listener = null;
            }
        }
    }

    public void setupMessageOrigins(ArrayList<String> trustedMsgOrgs) {
        trustedMessageOrigins.clear();
        if (trustedMsgOrgs.size() == 0) {
            removeAllOrigins();
        } else {
            for (String origin : trustedMsgOrgs) {
                addMessageOrigin(origin);
            }
        }
    }

    public void addMessageOrigin(String origin) {
        if (listener == null) {
            listener = addMessageHandler();
        }
        trustedMessageOrigins.add(origin);
        if (!cachedMessages.isEmpty() && cacheMessages) {
            for (Iterator<Message> i = cachedMessages.iterator(); i.hasNext();) {
                try {
                    Message m = i.next();
                    if (m.origin.equals(origin)) {
                        for (PostMessageReceiverHandler handler : handlers) {
                            handler.onMessage(m);
                        }
                        i.remove();
                        messageList.add(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeAllOrigins() {
        if (!cacheMessages) {
            removeMessageHandler(listener);
            listener = null;
        }
    }

    protected boolean checkOrigin(String origin) {
        return cacheMessages || trustedMessageOrigins.contains(origin);
    }

    protected void handleMessage(String message, String origin,
            JavaScriptObject src) {
        int messageId = messageCounter++;
        if (trustedMessageOrigins.contains(origin)) {
            final Message m = new Message(messageId, message, origin, src,
                    false);
            messageList.add(m);
            for (PostMessageReceiverHandler handler : handlers) {
                handler.onMessage(m);
            }
        } else if (cacheMessages) {
            cachedMessages.add(new Message(messageId, message, origin, src,
                    true));
        }
    }

    private JavaScriptObject listener;

    public native void postMessageToParent(String message, String targetOrigin)
    /*-{
        $wnd.parent.postMessage(message, targetOrigin);
    }-*/;

    private native JavaScriptObject addMessageHandler()
    /*-{
        var self = this;
        var receiver = $entry(function(event)
        {
            var origin = event.origin;
            var ok = self.@com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverWidget::checkOrigin(Ljava/lang/String;)(origin);
            var src = event.source;
            if (ok)
            {
                var message = event.data;
                self.@com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiverWidget::handleMessage(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(message,origin,src);
            }
        });
        if ($wnd.addEventListener) {
            $wnd.addEventListener('message', receiver, false);
        } else {
            $wnd.attachEvent('onmessage', receiver);
        }
        return receiver; 
    }-*/;

    private native void removeMessageHandler(JavaScriptObject listener)
    /*-{
        if ($wnd.removeEventListener) {
            $wnd.removeEventListener('message', listener, false);
        } else {
            $wnd.detachEvent('onmessage', listener);
        }
    }-*/;

    private native void respondToMessage(JavaScriptObject source,
            boolean parent, String message, String origin)
    /*-{
    	if (parent)
    	{
    		source.parent.postMessage(message, origin);
    	} else 
    	{
    		source.postMessage(message, origin);
    	}
    }-*/;

}
