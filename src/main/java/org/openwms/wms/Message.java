/*
 * Copyright 2005-2020 the original author or authors.
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
package org.openwms.wms;

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

    /** Message text about the {@literal Message}. */
    @Column(name = "C_MESSAGE")
    private String message;

    /** The unique key of the domain object in that context the message occurred. */
    private String pKey;

    /* ----------------------------- methods ------------------- */
    /**
     * Dear JPA...
     */
    protected Message() {
    }

    private Message(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        message = builder.message;
        pKey = builder.pKey;
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
    public String getMessage() {
        return message;
    }

    /**
     * Get the pKey.
     *
     * @return The pKey.
     */
    public String getpKey() {
        return pKey;
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
                Objects.equals(message, message1.message) &&
                Objects.equals(pKey, message1.pKey);
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
                .add("message='" + message + "'")
                .toString();
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(occurred, messageNo, message, pKey);
    }


    /**
     * {@code Message} builder static inner class.
     */
    public static final class Builder {

        private Date occurred;
        private String messageNo;
        private String message;
        private String pKey;

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
         * Sets the {@code message} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code message} to set
         * @return a reference to this Builder
         */
        public Builder withMessage(String val) {
            message = val;
            return this;
        }

        /**
         * Sets the {@code pKey} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code pKey} to set
         * @return a reference to this Builder
         */
        public Builder withPKey(String val) {
            pKey = val;
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