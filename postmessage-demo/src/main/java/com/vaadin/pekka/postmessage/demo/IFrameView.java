package com.vaadin.pekka.postmessage.demo;

import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.pekka.postmessage.PostMessageIFrame;
import com.vaadin.pekka.postmessage.PostMessageIFrame.PostMessageIFrameEvent;
import com.vaadin.pekka.postmessage.PostMessageIFrame.PostMessageIFrameListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class IFrameView extends HorizontalLayout implements View {

    private PostMessageIFrame iframe;
    private ComboBox locationSelect;
    private BeanItemContainer<PostMessageBean> messageContainer;
    private Grid grid;
    private VerticalLayout left;

    public IFrameView() {
        setSizeFull();
        setSpacing(true);

        Label label = new Label("PostMessageIFrame Testing");
        label.addStyleName("h3");
        locationSelect = createIFrameSelect();

        left = new VerticalLayout();
        left.setSizeFull();
        left.setSpacing(true);
        left.setMargin(true);
        left.addComponent(label);
        left.addComponent(locationSelect);
        addComponent(left);

        messageContainer = new BeanItemContainer<PostMessageBean>(
                PostMessageBean.class);
        grid = createGrid(messageContainer);

        locationSelect.setValue(PostmessageDemoUI.iframeOrigin3);
    }

    @Override
    public void enter(ViewChangeEvent event) {

    }

    public static Grid createGrid(Indexed messageContainer) {
        final Grid grid = new Grid();
        grid.setCaption("Received messages from iframe");
        grid.setSizeFull();
        grid.setContainerDataSource(messageContainer);
        grid.getColumn("messageId").setHeaderCaption("Id");
        grid.getColumn("message").setExpandRatio(2);
        grid.getColumn("origin").setExpandRatio(1);
        grid.getColumn("messageId").setWidth(40);
        for (Column c : grid.getColumns()) {
            c.setHidable(true);
        }

        return grid;
    }

    private void createIFrame() {
        iframe = new PostMessageIFrame();
        iframe.setSizeFull();
        iframe.setAlwaysListen(true);
        iframe.addPostMessageListener(new PostMessageIFrameListener() {

            @Override
            public void onMessage(PostMessageIFrameEvent event) {
                messageContainer.addBean(new PostMessageBean(event));
            }
        });

        final TextArea textArea = new TextArea(
                "Type text to send to IFrame application");
        textArea.setRows(2);
        textArea.setWidth("100%");
        textArea.setValue("Hello there (sent before load)");
        HorizontalLayout messageLayout = new HorizontalLayout();
        messageLayout.setWidth("100%");
        messageLayout.addComponent(textArea);
        messageLayout.setExpandRatio(textArea, 1.0F);
        Button sendButton = new Button("Send text to IFrame",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        iframe.postMessageToIFrame(textArea.getValue(), true);
                    }
                });
        sendButton.addStyleName("primary");
        messageLayout.addComponent(sendButton);
        messageLayout
                .setComponentAlignment(sendButton, Alignment.MIDDLE_CENTER);

        left.addComponent(messageLayout);
        left.addComponent(grid);
        left.setExpandRatio(grid, 1.0F);

        addComponent(iframe);
    }

    private ComboBox createIFrameSelect() {
        final ComboBox cb = createSelect("Open iframe for location");
        cb.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (iframe == null) {
                    createIFrame();
                }

                if (cb.getValue().equals(PostmessageDemoUI.iframeOrigin2)) {
                    iframe.setIframeOrigin(PostmessageDemoUI.iframeOrigin2);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(
                            PostmessageDemoUI.iframeLocation2));
                } else if (cb.getValue()
                        .equals(PostmessageDemoUI.iframeOrigin1)) {
                    iframe.setIframeOrigin(PostmessageDemoUI.iframeOrigin1);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(
                            PostmessageDemoUI.iframeLocation1));
                } else if (cb.getValue()
                        .equals(PostmessageDemoUI.iframeOrigin3)) {
                    iframe.setIframeOrigin(PostmessageDemoUI.iframeOrigin3);
                    iframe.postMessageToIFrame(
                            "Hello there (sent before load)", false);
                    iframe.setSource(new ExternalResource(
                            PostmessageDemoUI.iframeLocation3));
                }
            }
        });
        return cb;
    }

    private ComboBox createSelect(String caption) {
        final ComboBox cb = new ComboBox(caption);
        cb.setWidth("400px");
        cb.setNullSelectionAllowed(false);
        cb.setImmediate(true);
        cb.addItem(PostmessageDemoUI.iframeOrigin1);
        cb.addItem(PostmessageDemoUI.iframeOrigin2);
        cb.addItem(PostmessageDemoUI.iframeOrigin3);
        return cb;
    }

    public static class PostMessageBean {
        private String message;
        private Integer messageId;
        private String origin;

        public PostMessageBean(PostMessageIFrameEvent event) {
            message = event.getMessage();
            messageId = event.getMessageId();
            origin = event.getOrigin();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getMessageId() {
            return messageId;
        }

        public void setMessageId(Integer messageId) {
            this.messageId = messageId;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

    }
}
