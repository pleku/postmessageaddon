package com.example.postmessagereceivertest;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageReceiverEvent;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@Theme("tests-valo-flatdark")
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
        layout.setSpacing(true);
        setContent(layout);

        Label label = new Label("PostMessageReceiver Testing @ "
                + getPage().getLocation());
        label.addStyleName("h3");
        layout.addComponent(label);

        final PostMessageReceiverExtension receiver = new PostMessageReceiverExtension();
        addExtension(receiver);
        receiver.addAcceptedMessageOrigin("http://localhost:8088");
        receiver.addAcceptedMessageOrigin("http://pekka.virtuallypreinstalled.com");
        receiver.addAcceptedMessageOrigin("http://pekka.app.fi");

        final TextArea messageField = new TextArea(
                "Type text to send to parent frame / window");
        messageField.setWidth("100%");
        messageField.setRows(2);
        layout.addComponent(messageField);
        Button parentButton = new Button("Send text to window.parent",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        receiver.postMessageToParent(messageField.getValue(),
                                "*");
                    }
                });
        parentButton.addStyleName("primary");
        Button openerButton = new Button("Send text to window.opener",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        receiver.postMessageToOpener(messageField.getValue(),
                                "*");
                    }
                });
        openerButton.addStyleName("friendly");
        HorizontalLayout horizontalLayout = new HorizontalLayout(parentButton,
                openerButton);
        HorizontalLayout buttons = horizontalLayout;
        buttons.setSpacing(true);
        layout.addComponent(buttons);

        final CheckBox acceptMessages = new CheckBox("Accept messages", true);
        acceptMessages.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (acceptMessages.getValue()) {
                    receiver.addAcceptedMessageOrigin("http://localhost:8088");
                    receiver.addAcceptedMessageOrigin("http://pekka.virtuallypreinstalled.com");
                    receiver.addAcceptedMessageOrigin("http://pekka.app.fi");
                } else {
                    receiver.clearAllAcceptedMessageOrigins();
                }
            }
        });

        layout.addComponent(acceptMessages);

        final BeanItemContainer<PostMessageReceiverEvent> messageContainer = new BeanItemContainer<PostMessageReceiverEvent>(
                PostMessageReceiverEvent.class);
        Grid grid = new Grid();
        grid.setCaption("Received messages");
        grid.setSizeFull();
        grid.setContainerDataSource(messageContainer);
        grid.getColumn("connector").setHidden(true);
        grid.getColumn("source").setHidden(true);
        grid.getColumn("messageId").setHeaderCaption("Id");
        grid.getColumn("message").setExpandRatio(2);
        grid.getColumn("origin").setExpandRatio(1);
        grid.getColumn("messageId").setWidth(40);
        grid.getColumn("component").setHidden(true);
        for (Column c : grid.getColumns()) {
            c.setHidable(true);
        }
        grid.setSelectionMode(SelectionMode.NONE);
        layout.addComponent(grid);

        receiver.addListener(new PostMessageListener() {

            public void onMessage(PostMessageReceiverEvent event) {
                messageContainer.addBean(event);
            }
        });

    }
}