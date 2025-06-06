/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms.transactions.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import org.ameba.http.AbstractBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A TransactionVO.
 *
 * @author Heiko Scherrer
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransactionVO extends AbstractBase<TransactionVO> {

    /** HTTP media type representation. */
    public static final String MEDIA_TYPE = "application/vnd.openwms.common.transaction-v1+json";

    /** The persistent key. */
    @Null(groups = {ValidationGroups.Create.class})
    @JsonProperty("pKey")
    @Size(max = 255)
    private String pKey;

    /** A human readable descriptive text of the Transaction that happened. */
    @JsonProperty("description")
    @Size(max = 2048)
    private String description;

    /** A defined transaction type or key. */
    @NotEmpty(groups = {ValidationGroups.Create.class})
    @JsonProperty("type")
    @Size(max = 255)
    private String type;

    /** The human user or system that initiated the Transaction. */
    @JsonProperty("createdByUser")
    @Size(max = 255)
    private String createdByUser;

    /** The service name that created the Transaction. */
    @JsonProperty("sender")
    @Size(max = 255)
    private String sender;

    /** An id to a dataset the Transaction is related to. */
    @JsonProperty("senderId")
    @Size(max = 255)
    private String senderId;

    /** A category name, like Reconciliation or Replenishment the Transaction is part of. */
    @JsonProperty("category")
    @Size(max = 255)
    private String category;

    /** An id to request (business transaction) the Transaction was part of. */
    @JsonProperty("traceId")
    @Size(max = 255)
    private String traceId;

    /** Arbitrary detail information with values according to the specific Transaction. */
    @JsonProperty("details")
    private Map<String, String> details;


    public String getpKey() {
        return pKey;
    }

    public void setpKey(String pKey) {
        this.pKey = pKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public void addDetail(String key, String value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionVO)) return false;
        if (!super.equals(o)) return false;
        TransactionVO that = (TransactionVO) o;
        return Objects.equals(pKey, that.pKey) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(createdByUser, that.createdByUser) && Objects.equals(sender, that.sender) && Objects.equals(senderId, that.senderId) && Objects.equals(category, that.category) && Objects.equals(traceId, that.traceId) && Objects.equals(details, that.details);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pKey, description, type, createdByUser, sender, senderId, category, traceId, details);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", TransactionVO.class.getSimpleName() + "[", "]")
                .add("pKey='" + pKey + "'")
                .add("description='" + description + "'")
                .add("type='" + type + "'")
                .add("createdByUser='" + createdByUser + "'")
                .add("sender='" + sender + "'")
                .add("senderId='" + senderId + "'")
                .add("category='" + category + "'")
                .add("traceId='" + traceId + "'")
                .add("details=" + details)
                .toString();
    }
}
