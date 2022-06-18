package com.netdisk.module.DTO;

import com.netdisk.module.chat.ChatInfo;
import com.netdisk.module.chat.Message;
import com.netdisk.module.chat.MessageFile;
import com.netdisk.module.chat.Reactions;
import lombok.Data;

import java.util.List;

@Data
public class MessageDTO {

    long _id;

    long indexId;

    long roomId;

    long senderId;

    String content;

    String username;

    String avatar;

    String date;

    String timestamp;

    //8个属性
    Boolean system;

    Boolean saved;

    Boolean distributed;

    Boolean seen;

    Boolean deleted;

    Boolean failure;

    Boolean disableActions;

    Boolean disableReactions;

    MessageFile messageFile;

    Reactions reactions;

    public MessageDTO(Message message, ChatInfo chatInfo) {
        this._id = message.getMessageId();
        this.indexId = message.getMessageId();
        this.roomId = message.getRoomId();
        this.senderId = message.getSenderId();
        this.content = message.getContent();
        this.username = message.getUserName();
        ////todo change this
        this.avatar = "http://192.168.1.143:9090/vavatar/" + message.getAvatar() + "?time=" + "time";
        this.date = message.getDate();
        this.timestamp = message.getTimestamp();
        this.system = chatInfo.getSystem();
        this.saved = chatInfo.getSaved();
        this.distributed = chatInfo.getDistributed();
        this.seen = chatInfo.getSeen();
        this.deleted = chatInfo.getDeleted();
        this.failure = chatInfo.getFailure();
        this.disableActions = chatInfo.getDisableActions();
        this.disableReactions = chatInfo.getDisableReactions();
        messageFile = message.getMessageFile();
        this.reactions = message.getReactions();
    }

}
