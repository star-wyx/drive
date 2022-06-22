package com.netdisk.module.DTO;

import com.netdisk.module.chat.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
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

    ReplyMessage replyMessage;

}
