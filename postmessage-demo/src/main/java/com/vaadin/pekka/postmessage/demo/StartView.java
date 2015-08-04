package com.vaadin.pekka.postmessage.demo;

import org.vaadin.teemu.VaadinIcons;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class StartView extends VerticalLayout implements View {

    @Override
    public void enter(ViewChangeEvent event) {

    }

    public StartView() {
        setSizeFull();
        Label label = new Label("Select the messaging type to test");
        label.addStyleName("h2");
        label.setWidth(null);
        addComponent(label);
        setComponentAlignment(label, Alignment.BOTTOM_CENTER);

        Button iframeButton = new Button("IFrame", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                getUI().getNavigator().navigateTo("iframe");
            }
        });
        iframeButton.addStyleName("huge");
        iframeButton.setWidth("300px");
        iframeButton.setIcon(VaadinIcons.LAYOUT);

        Button windowButton = new Button("Pop-up Window",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        getUI().getNavigator().navigateTo("popup-window");
                    }
                });
        windowButton.addStyleName("huge");
        windowButton.setWidth("300px");
        windowButton.setIcon(VaadinIcons.EXTERNAL_BROWSER);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setWidth("100%");
        buttons.addComponent(iframeButton);
        buttons.setComponentAlignment(iframeButton, Alignment.MIDDLE_RIGHT);
        buttons.addComponent(windowButton);
        buttons.setComponentAlignment(windowButton, Alignment.MIDDLE_LEFT);

        addComponent(buttons);
        setComponentAlignment(buttons, Alignment.TOP_CENTER);
    }

}