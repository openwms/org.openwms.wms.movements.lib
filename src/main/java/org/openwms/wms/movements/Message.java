/*
 * Copyright 2005-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.wms.movements;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A Message is used to encapsulate a message text with an identifier.
 * 
 * @GlossaryTerm
 * @author Heiko Scherrer
 */
@Embeddable
public class Message implements Serializable {

    /** Timestamp when the {@literal Message} has occurred. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "C_OCCURRED")
    private Date occurred;

    /** Message number of the {@literal Message}. */
    @Column(name = "C_NO")
    private String messageNo;

    /** Message text of the {@literal Message}. */
    @Column(name = "C_MESSAGE", length = DEF_MESSAGE_LENGTH)
    private String messageText;
    /** Default length of {@code message}. */
    public static final int DEF_MESSAGE_LENGTH = 1024;

    /* ----------------------------- methods ------------------- */
    /**
     * Dear JPA...
     */
    protected Message() {
    }

    private Message(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        if (builder.messageText != null) {
            messageText = builder.messageText.length() > DEF_MESSAGE_LENGTH ? builder.messageText.substring(0, DEF_MESSAGE_LENGTH-1) : builder.messageText;
        }
    }

    /**
     * Return the Date when the {@literal Message} has occurred.
     * 
     * @return Date when occurred.
     */
    public Date getOccurred() {
        return occurred;
    }

    /**
     * Get the messageNo.
     * 
     * @return The messageNo.
     */
    public String getMessageNo() {
        return messageNo;
    }

    /**
     * Get the message.
     * 
     * @return The message.
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(occurred, message1.occurred) &&
                Objects.equals(messageNo, message1.messageNo) &&
                Objects.equals(messageText, message1.messageText);
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("occurred=" + occurred)
                .add("messageNo='" + messageNo + "'")
                .add("messageText='" + messageText + "'")
                .toString();
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(occurred, messageNo, messageText);
    }


    /**
     * {@code Message} builder static inner class.
     */
    public static final class Builder {

        private Date occurred;
        private String messageNo;
        private String messageText;

        /**
         * Sets the {@code occurred} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code occurred} to set
         * @return a reference to this Builder
         */
        public Builder withOccurred(Date val) {
            occurred = val;
            return this;
        }

        /**
         * Sets the {@code messageNo} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code messageNo} to set
         * @return a reference to this Builder
         */
        public Builder withMessageNo(String val) {
            messageNo = val;
            return this;
        }

        /**
         * Sets the {@code messageText} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code messageText} to set
         * @return a reference to this Builder
         */
        public Builder withMessageText(String val) {
            messageText = val;
            return this;
        }

        /**
         * Returns a {@code Message} built from the parameters previously set.
         *
         * @return a {@code Message} built with parameters of this {@code Message.Builder}
         */
        public Message build() {
            return new Message(this);
        }
    }
}