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
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.vaadin.client.BrowserInfo;

public class PostMessageReceiver implements HasPostMessageHandlers {

    protected final List<String> trustedMessageOrigins;

    protected final List<PostMessage> cachedMessages;

    protected List<PostMessage> messageList;

    protected int messageCounter = 0;

    protected final boolean isIE8;

    boolean cacheMessages = true;

    private HandlerManager handlerManager;

    public PostMessageReceiver() {
        trustedMessageOrigins = new ArrayList<String>();
        cachedMessages = new ArrayList<PostMessage>();
        messageList = new ArrayList<PostMessage>();
        listener = addMessageHandler();
        isIE8 = BrowserInfo.get().isIE8();
    }

    /**
     * Ensures the existence of the handler manager.
     *
     * @return the handler manager
     * */
    HandlerManager ensureHandlers() {
        return handlerManager == null ? handlerManager = new HandlerManager(
                this) : handlerManager;
    }

    public HandlerRegistration addPostMessageHandler(PostMessageHandler handler) {
        return ensureHandlers()
                .addHandler(
                        com.vaadin.pekka.postmessage.client.receiver.PostMessageEvent
                                .getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (handlerManager != null) {
            handlerManager.fireEvent(event);
        }
    }

    public void respondToMessage(int msgId, String msg, boolean parent) {
        for (PostMessage m : messageList) {
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
            for (Iterator<PostMessage> i = cachedMessages.iterator(); i
                    .hasNext();) {
                try {
                    PostMessage m = i.next();
                    if (m.origin.equals(origin)) {
                        fireEvent(new PostMessageEvent(m));
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
            final PostMessage m = new PostMessage(messageId, message, origin,
                    src, false);
            messageList.add(m);
            fireEvent(new PostMessageEvent(m));
        } else if (cacheMessages) {
            cachedMessages.add(new PostMessage(messageId, message, origin, src,
                    true));
        }
    }

    /**
     *
     */
    public void remove() {
        if (listener != null) {
            removeMessageHandler(listener);
        }
        if (cachedMessages != null) {
            cachedMessages.clear();
        }
        if (messageList != null) {
            messageList.clear();
        }
        if (trustedMessageOrigins != null) {
            trustedMessageOrigins.clear();
        }
    }

    private JavaScriptObject listener;

    public native void postMessageToParent(String message, String targetOrigin)
    /*-{
        if ($wnd.parent) {
            $wnd.parent.postMessage(message, targetOrigin);
        }
    }-*/;

    public native void postMessageToOpener(String message, String targetOrigin)
    /*-{
        debugger;
        if ($wnd.opener) {
            $wnd.opener.postMessage(message, targetOrigin);
        }
     }-*/;

    private native JavaScriptObject addMessageHandler()
    /*-{
        var self = this;
        var receiver = $entry(function(event)
        {
            var origin = event.origin;
            var ok = self.@com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiver::checkOrigin(Ljava/lang/String;)(origin);
            var src = event.source;
            if (ok)
            {
                var message = event.data;
                self.@com.vaadin.pekka.postmessage.client.receiver.PostMessageReceiver::handleMessage(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(message,origin,src);
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
