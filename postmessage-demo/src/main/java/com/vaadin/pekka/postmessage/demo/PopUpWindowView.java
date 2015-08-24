package com.vaadin.pekka.postmessage.demo;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.pekka.postmessage.PostMessageEvent;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageListener;
import com.vaadin.pekka.postmessage.PostMessageReceiverExtension.PostMessageReceiverEvent;
import com.vaadin.pekka.postmessage.PostMessageWindowExtension;
import com.vaadin.pekka.postmessage.PostMessageWindowExtension.PostMessageWindow;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class PopUpWindowView extends VerticalLayout implements View {

    private PostMessageWindowExtension opener;
    private BeanItemContainer<PostMessageEvent> messageContainer;
    private HorizontalLayout layout;

    public PopUpWindowView() {
        Button openButton = new Button("Click to open window for messaging",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        openPopup();
                    }
                });
        openButton.addStyleName("huge");
        openButton.addStyleName("primary");
        addComponent(openButton);
        setComponentAlignment(openButton, Alignment.MIDDLE_CENTER);
        layout = new HorizontalLayout();
        layout.setSizeFull();
        setSizeFull();
        setMargin(true);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        if (opener == null) {
            opener = new PostMessageWindowExtension(getUI());
        }
    }

    private void openPopup() {

        final PostMessageWindow openWindow = opener
                .openWindow(PostmessageDemoUI.iframeLocation3,
                        PostmessageDemoUI.iframeOrigin3,
                        "width=650, height=650, top=100, left=100, titlebar=0, status=0, toolbar=0");

        final TextArea messageField = new TextArea(
                "Type text to send to window");
        messageField.setValue("Hello there (sent before load)");
        messageField.setWidth("300px");
        messageField.setRows(2);

        Label label = new Label("Window id:" + openWindow.getId());
        label.addStyleName("h2");

        Button sendButton = new Button("Send text to window "
                + openWindow.getId(), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                openWindow.postMessage(messageField.getValue());
            }
        });
        sendButton.addStyleName("friendly");

        final BeanItemContainer<PostMessageEvent> container = new BeanItemContainer<PostMessageEvent>(
                PostMessageEvent.class);
        Grid grid = IFrameView.createGrid(container);
        grid.getColumn("source").setHidden(true);
        grid.getColumn("connector").setHidden(true);
        grid.getColumn("component").setHidden(true);
        grid.setCaption("Received messages from window " + openWindow.getId());

        openWindow.addPostMessageListener(new PostMessageListener() {

            @Override
            public void onMessage(PostMessageReceiverEvent event) {
                container.addBean(event);
            }
        });

        VerticalLayout popupLayout = new VerticalLayout();
        popupLayout.addComponent(label);
        HorizontalLayout messageLayout = new HorizontalLayout();
        messageLayout.addComponent(messageField);
        messageLayout.addComponent(sendButton);
        messageLayout
                .setComponentAlignment(messageField, Alignment.MIDDLE_LEFT);
        messageLayout
                .setComponentAlignment(sendButton, Alignment.MIDDLE_CENTER);
        popupLayout.addComponent(messageLayout);
        popupLayout.addComponent(grid);
        popupLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);
        popupLayout.setSpacing(true);
        popupLayout.setMargin(true);

        layout.addComponent(popupLayout);
        layout.setComponentAlignment(popupLayout, Alignment.MIDDLE_CENTER);
        addComponent(layout);
        setExpandRatio(layout, 1.0F);

        openWindow.postMessage(messageField.getValue());
    }
}
