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
package com.vaadin.pekka.postmessage.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@Title("PostMessageIFrame Add-on Demo")
@SuppressWarnings("serial")
@Theme("tests-valo-flat")
public class PostmessageDemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = PostmessageDemoUI.class, widgetset = "com.vaadin.pekka.postmessage.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    static final String iframeOrigin1 = "http://pekka.virtuallypreinstalled.com";
    static final String iframeLocation1 = "http://pekka.virtuallypreinstalled.com/postmessage-receiver-demo";
    static final String iframeOrigin2 = "http://pekka.app.fi";
    static final String iframeLocation2 = "http://pekka.app.fi/postmessage-receiver-demo";
    static final String iframeOrigin3 = "http://localhost:8080";
    static final String iframeLocation3 = "http://localhost:8080/postmessage-receiver-demo";

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        setContent(layout);

        Navigator navigator = new Navigator(this, layout);
        navigator.addView("", new StartView());
        navigator.addView("iframe", IFrameView.class);
        navigator.addView("popup-window", PopUpWindowView.class);
    }

    private ComboBox createSelect(String caption) {
        final ComboBox cb = new ComboBox(caption);
        cb.setNullSelectionAllowed(false);
        cb.setImmediate(true);
        cb.addItem(iframeOrigin1);
        cb.addItem(iframeOrigin2);
        cb.addItem(iframeOrigin3);
        cb.setValue(iframeOrigin1);
        return cb;
    }
}
