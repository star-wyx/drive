package com.netdisk.module.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastMessage {

    String content;

    long senderId;

    /**
     * messageId
     */
    long _id;

    String username;

    String timestamp;

    Boolean saved;

    Boolean distributed;

    Boolean seen;

    @JsonProperty("new")
    Boolean isNew;

    public LastMessage(Message message, ChatInfo chatInfo, boolean isNew) {
        this.content = message.getContent();
        this.senderId = message.getSenderId();
        this._id = message.getMessageId();
        this.username = message.getUserName();
        this.timestamp = message.getTimestamp();
        this.saved = chatInfo.getSaved();
        this.distributed = chatInfo.getDistributed();
        this.seen = chatInfo.getSeen();
        this.isNew = isNew;
    }

}
