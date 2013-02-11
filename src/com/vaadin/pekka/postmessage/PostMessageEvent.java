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

import com.vaadin.ui.Component;

/**
 * Represents the basic properties of a Post Message event.
 * 
 * @author pekkahyvonen
 * 
 */
@SuppressWarnings("serial")
public abstract class PostMessageEvent extends Component.Event {

    private final String origin;
    private final String message;
    private final Integer messageId;

    public PostMessageEvent(final Component source, final String origin,
            final String message, final Integer messageId) {
        super(source);
        this.origin = origin;
        this.message = message;
        this.messageId = messageId;
    }

    /**
     * 
     * @return the origin where the message came from
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * 
     * @return the actual message
     */
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @return an id for the message, or -1 if component doesn't identify
     *         messages with ids
     */
    public Integer getMessageId() {
        return messageId;
    }

    // for responding to the specific message this event represents
    public abstract void respond(final String message, final boolean option);

}
