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
package com.vaadin.pekka.postmessage.client.iframe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.IFrameElement;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ui.VBrowserFrame;

public class PostMessageIFrameWidget extends VBrowserFrame {

    public interface PostMessageIFrameWidgetHandler {
        public void onMessage(String message, String origin);
    }

    public static final String CLASSNAME = "postmessage-iframe";

    boolean iframeLoaded = false;
    boolean alwaysListen = false;
    String iframeOrigin = null;

    protected int pendingResponses = 0;
    protected final boolean isIE8;
    protected List<String[]> waitingMessages;
    protected List<PostMessageIFrameWidgetHandler> handlers;

    protected JavaScriptObject listener;

    protected String src;

    public PostMessageIFrameWidget() {
        super();

        isIE8 = BrowserInfo.get().isIE8();
        waitingMessages = new ArrayList<String[]>();
        handlers = new ArrayList<PostMessageIFrameWidgetHandler>();
    }

    public void addHandler(PostMessageIFrameWidgetHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(PostMessageIFrameWidgetHandler handler) {
        handlers.remove(handler);
    }

    protected void setupListener() {
        if ((pendingResponses > 0 || alwaysListen) && listener == null) {
            listener = addResponseListener();
        } else if (pendingResponses == 0 && !alwaysListen && listener != null) {
            removeResponseListener(listener);
            listener = null;
        }
    }

    @Override
    protected IFrameElement createIFrameElement(String src) {
        iframeLoaded = false;
        this.src = src;
        final IFrameElement createIFrameElement = super
                .createIFrameElement(src);
        addOnLoadHandler(createIFrameElement.cast());
        return createIFrameElement;
    }

    @Override
    protected void createAltTextElement() {
        iframeLoaded = false;
        super.createAltTextElement();
    }

    protected void clearListener() {
        listener = null;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        iframeLoaded = false;
        if (listener != null) {
            removeResponseListener(listener);
            listener = null;
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        setupListener();
    }

    protected void postMessageToIFrame(String message, boolean listenToResponse) {
        if (listenToResponse && !alwaysListen) {
            pendingResponses++;
            setupListener();
        }
        if (iframeLoaded) {
            postMessageToIFrame(iframe.cast(), message, iframeOrigin);
        } else {
            waitingMessages.add(new String[] { message, iframeOrigin });
        }
    }

    void receiveResponse(String message, String receivedOrigin) {
        if ((alwaysListen || pendingResponses > 0) && receivedOrigin != null) {
            for (PostMessageIFrameWidgetHandler handler : handlers) {
                handler.onMessage(message, receivedOrigin);
            }
            if (!alwaysListen) {
                pendingResponses--;
            }
        }
    }

    boolean shouldRemoveListener() {
        return !alwaysListen && pendingResponses == 0;
    }

    boolean isExpected(String receivedOrigin) {
        return iframeOrigin != null && !iframeOrigin.isEmpty()
                && iframeOrigin.equals(receivedOrigin);
    }

    void onIFrameLoad(String str) {
        if (src != null && (src.equals(str) || str.equals("ie"))) {
            iframeLoaded = true;
            if (!waitingMessages.isEmpty()) {
                schedulePostMessagesAfterDelay();
            }
        }
    }

    protected void schedulePostMessagesAfterDelay() {
        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {

            public boolean execute() {
                for (Iterator<String[]> i = waitingMessages.iterator(); i.hasNext();) {
                    String[] s = i.next();
                    postMessageToIFrame(iframe.cast(), s[0], s[1]);
                    i.remove();
                }
                return false;
            }
        }, 1000);
    }

    protected native void addOnLoadHandler(JavaScriptObject iframe)
    /*-{
        var self = this;
        var onloadfunct = function(event) {
            var str;
            if (event && event.target && event.target.src) {
                str = event.target.src;
            } else if (event && event.srcElement && event.srcElement.src) {
                str = event.srcElement.src;
            } else {
                str = 'ie';
            }
            if (str && (0 !== str.length) ) 
                self.@com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameWidget::onIFrameLoad(Ljava/lang/String;)(str);
        };
        if (iframe.addEventListener)
            iframe.addEventListener('load', onloadfunct, false);
        else if (iframe.attachEvent)
            iframe.attachEvent('onload', onloadfunct);
        else
            iframe.onload = onloadfunct;
    }-*/;

    protected native void postMessageToIFrame(JavaScriptObject iframe,
            String message, String targetOrigin)
    /*-{
        var target = iframe.contentWindow;
        target.postMessage(message, targetOrigin);
    }-*/;

    protected native void setIFrameLocation(JavaScriptObject iframe)
    /*-{
        var src = iframe.src;
        iframe.contentWindow.location.href= src;
        
    }-*/;

    protected native JavaScriptObject addResponseListener()
    /*-{
        var self = this;
        var respFunc = $entry(function(event)
        {
            var org = event.origin;
            var expected = self.@com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameWidget::isExpected(Ljava/lang/String;)(org);
            if (expected)
            {
            	var message = event.data;
            	self.@com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameWidget::receiveResponse(Ljava/lang/String;Ljava/lang/String;)(message,org);
            }
            self.@com.vaadin.pekka.postmessage.client.iframe.PostMessageIFrameWidget::setupListener()();
        });
        if ($wnd.addEventListener) {
            $wnd.addEventListener('message', respFunc, false);
        } else {
            $wnd.attachEvent('onmessage', respFunc);
        };
        return respFunc;
    }-*/;

    protected native void removeResponseListener(JavaScriptObject l)
    /*-{
        if ($wnd.addEventListener) {
    	    $wnd.removeEventListener('message', l, false);
    	} else {
    	    $wnd.detachevent('onmessage', l);
    	}
    }-*/;

}
