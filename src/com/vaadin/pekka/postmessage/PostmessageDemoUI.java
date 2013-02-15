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
package com.vaadin.pekka.postmessage;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.pekka.postmessage.PostMessageIFrame.PostMessageIFrameEvent;
import com.vaadin.pekka.postmessage.PostMessageIFrame.PostMessageIFrameListener;
import com.vaadin.pekka.postmessage.PostMessageWindowUtil.PostMessageWindow;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class PostmessageDemoUI extends UI {

    private final String iframeOrigin1 = "http://pekka.virtuallypreinstalled.com";
    private final String iframeLocation1 = "http://pekka.virtuallypreinstalled.com/PostMessageReceiverTest";
    private final String iframeOrigin2 = "http://pekkahyvonen.com";
    private final String iframeLocation2 = "http://pekkahyvonen.com/postmessage_app_test.html";
    private final String iframeOrigin3 = "http://localhost:8080";
    private final String iframeLocation3 = "http://localhost:8080/PostMessageReceiverTest";

    @Override
    protected void init(VaadinRequest request) {
        final HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        setContent(content);

        final Panel messages = new Panel("Received message events");
        messages.setContent(new VerticalLayout());
        messages.setWidth("400px");
        messages.setHeight("500px");
        content.addComponent(messages);

        final VerticalLayout layout = new VerticalLayout();
        content.addComponent(layout);

        final PostMessageWindowUtil windowOpener = new PostMessageWindowUtil();
        windowOpener.extend(this);

        final HorizontalLayout windowops = new HorizontalLayout();
        final ComboBox winselect = new ComboBox();
        winselect.setImmediate(true);
        winselect.setNullSelectionAllowed(false);
        final Button postMessage = new Button("Post message to window",
                new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        Object value = winselect.getValue();
                        if (value != null && value instanceof PostMessageWindow) {
                            ((PostMessageWindow) value)
                                    .postMessage("respond, message from parent");
                        }
                    }
                });

        final ComboBox winLocSelect = createSelect("Select window location&origin");
        windowops.addComponent(winLocSelect);
        windowops.addComponent(new Button("Add new PostMessageWindow",
                new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        PostMessageWindow window;
                        if (winLocSelect.getValue().equals(iframeOrigin2)) {
                            window = windowOpener.openWindow(iframeLocation2,
                                    iframeOrigin2, "width=400,height=400");
                        } else if (winLocSelect.getValue()
                                .equals(iframeOrigin1)) {
                            window = windowOpener.openWindow(iframeLocation1,
                                    iframeOrigin1, "width=400,height=400");
                        } else {
                            window = windowOpener.openWindow(iframeLocation3,
                                    iframeOrigin3, "width=400,height=400");
                        }
                        winselect.addItem(window);
                    }
                }));
        windowops.addComponent(winselect);
        windowops.addComponent(postMessage);
        layout.addComponent(windowops);

        layout.addComponent(new Button("Add PostMessageIFrame",
                new Button.ClickListener() {

                    private PostMessageIFrame iframe;

                    public void buttonClick(ClickEvent event) {
                        final Button button = event.getButton();
                        layout.removeComponent(button);

                        iframe = new PostMessageIFrame(
                                "IFrame pointing to another Vaadin app (should be different origin)");

                        final VerticalLayout layout2 = new VerticalLayout();

                        layout2.addComponent(createIFrameSelect(iframe));

                        layout2.addComponent(new Button(
                                "Remove IFrame, but keep it",
                                new Button.ClickListener() {

                                    public void buttonClick(ClickEvent event) {
                                        if (event.getButton().getCaption()
                                                .equals("add")) {
                                            content.addComponent(iframe);
                                            event.getButton()
                                                    .setCaption(
                                                            "Remove IFrame, but keep it");
                                        } else {
                                            content.removeComponent(iframe);
                                            event.getButton().setCaption("add");
                                        }
                                    }
                                }));

                        layout2.addComponent(new Button(
                                "Remove IFrame & close",
                                new Button.ClickListener() {

                                    public void buttonClick(ClickEvent event) {
                                        iframe.postMessageToIFrame("close",
                                                false);
                                        layout.removeComponent(layout2);
                                        layout.removeComponent(event
                                                .getComponent());
                                        layout.addComponent(button);
                                        content.removeComponent(iframe);
                                    }
                                }));

                        layout.addComponent(layout2);

                        iframe.setId("PostMessageIFrame");
                        iframe.setWidth("400px");
                        iframe.setHeight("400px");
                        iframe.addListener(new PostMessageIFrameListener() {

                            public void onMessage(PostMessageIFrameEvent event) {
                                ((VerticalLayout) messages.getContent()).addComponent(new Label(
                                        event.getMessage() + ", "
                                                + event.getOrigin() + ", "
                                                + event.getComponent().getId()));
                                final String[] split = event.getMessage()
                                        .split(",");
                                if (split[0].equals("size")) {
                                    iframe.setWidth(split[1]);
                                    iframe.setHeight(split[2]);
                                }
                            }
                        });

                        layout2.addComponent(new Button(
                                "IFRAME: post msg and expect response",
                                new Button.ClickListener() {

                                    public void buttonClick(ClickEvent event) {
                                        iframe.postMessageToIFrame(
                                                "respond - state", true);
                                    }
                                }));
                        layout2.addComponent(new Button(
                                "IFRAME: Just post a msg",
                                new Button.ClickListener() {

                                    public void buttonClick(ClickEvent event) {
                                        iframe.postMessageToIFrame("SPAM",
                                                false);
                                    }
                                }));
                        layout2.addComponent(new Button(
                                "IFRAME: Request width&height",
                                new Button.ClickListener() {

                                    public void buttonClick(ClickEvent event) {
                                        iframe.postMessageToIFrame("size?",
                                                true);
                                    }
                                }));

                        final CheckBox ns = new CheckBox(
                                "Always Listen for iframe messages");
                        ns.setValue(false);
                        ns.setImmediate(true);
                        ns.addValueChangeListener(new Property.ValueChangeListener() {

                            public void valueChange(
                                    Property.ValueChangeEvent event) {
                                iframe.setAlwaysListen(ns.getValue());
                            }
                        });

                        layout2.addComponent(ns);

                        iframe.setAlternateText("Loading...");
                        layout2.addComponent(iframe);
                        iframe.setIframeOrigin(iframeOrigin1);
                        iframe.postMessageToIFrame(
                                "Hello there (sent before load)", false);
                        iframe.setSource(new ExternalResource(iframeLocation1));
                    }
                }));
    }

    private ComboBox createIFrameSelect(final PostMessageIFrame iframe) {
        final ComboBox cb = createSelect("Select IFrame location&origin");
        cb.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (cb.getValue().equals(iframeOrigin2)) {
                    iframe.setIframeOrigin(iframeOrigin2);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(iframeLocation2));
                } else if (cb.getValue().equals(iframeOrigin1)) {
                    iframe.setIframeOrigin(iframeOrigin1);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(iframeLocation1));
                } else {
                    iframe.setIframeOrigin(iframeOrigin3);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(iframeLocation3));
                }
            }
        });
        return cb;
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
