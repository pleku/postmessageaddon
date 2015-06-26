package com.example.postmessagereceivertest;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageReceiverEvent;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageReceiverListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class PostmessagereceivertestUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = PostmessagereceivertestUI.class, widgetset = "com.vaadin.pekka.postmessage.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        Label label = new Label("PostMessageReceiver testing");
        layout.addComponent(label);

        final Panel panel = new Panel("Received message events");
        final VerticalLayout messages = new VerticalLayout();
        panel.setContent(messages);

        final PostMessageReceiverExtension receiver = new PostMessageReceiverExtension();
        addExtension(receiver);

        final TextField messageField = new TextField("Type message here");
        layout.addComponent(messageField);

        receiver.addListener(new PostMessageReceiverListener() {

            public void onMessage(PostMessageReceiverEvent event) {
                messages.addComponentAsFirst(new Label(event.getMessage()
                        + ", " + event.getOrigin() + ", "
                        + event.getComponent().getId()));
                if (event.getMessage().equals("size?")) {
                    receiver.postMessageToParent(
                            "size,"
                                    + Integer.toString((int) (Math.random() * 600))
                                    + "px,"
                                    + Integer.toString((int) (Math.random() * 600))
                                    + "px", event.getOrigin());
                } else if (event.getMessage().startsWith("respond")) {
                    event.respond("Response is: " + messageField.getValue(),
                            true);
                } else if (event.getMessage().equals("close")) {
                    close();
                }
            }
        });

        Button button3 = new Button("post to $wnd.parent using executeJS");
        button3.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {

                getPage().getJavaScript().execute(
                        "window.parent.postMessage('" + messageField.getValue()
                                + "', 'http://localhost:8080');");
                getPage()
                        .getJavaScript()
                        .execute(
                                "window.parent.postMessage('"
                                        + messageField.getValue()
                                        + "', 'http://pekka.virtuallypreinstalled.com');");
                getPage().getJavaScript().execute(
                        "window.parent.postMessage('" + messageField.getValue()
                                + "', 'http://pekka.app.fi');");
            }
        });
        // layout.addComponent(button3);

        Button button4 = new Button("post to $wnd.parent using receiver");
        button4.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                receiver.postMessageToParent(messageField.getValue(),
                        "http://localhost:8080");
                receiver.postMessageToParent(messageField.getValue(),
                        "http://pekka.virtuallypreinstalled.com");
                receiver.postMessageToParent(messageField.getValue(),
                        "http://pekka.app.fi");
            }
        });
        layout.addComponent(button4);

        Button button5 = new Button("post to $wnd.opener using receiver");
        button5.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                receiver.postMessageToOpener(messageField.getValue(),
                        "http://localhost:8080");
                receiver.postMessageToOpener(messageField.getValue(),
                        "http://pekka.virtuallypreinstalled.com");
                receiver.postMessageToOpener(messageField.getValue(),
                        "http://pekka.app.fi");
            }
        });
        layout.addComponent(button5);

        final Button button2 = new Button(
                "add parent location to trusted origins");
        final Button button1 = new Button(
                "remove parent location from trusted origins");
        button2.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                receiver.addAcceptedMessageOrigin("http://localhost:8080");
                receiver.addAcceptedMessageOrigin("http://pekka.virtuallypreinstalled.com");
                receiver.addAcceptedMessageOrigin("http://pekka.app.fi");
                event.getButton().setEnabled(false);
                button1.setEnabled(true);
            }
        });
        layout.addComponent(button2);

        button1.setEnabled(false);
        button1.addClickListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                receiver.removeAcceptedMessageOrigin("http://localhost:8080");
                receiver.removeAcceptedMessageOrigin("http://pekka.virtuallypreinstalled.com");
                receiver.removeAcceptedMessageOrigin("http://pekka.app.fi");
                event.getButton().setEnabled(false);
                button2.setEnabled(true);
            }
        });
        layout.addComponent(button1);

        layout.addComponent(panel);

    }

}