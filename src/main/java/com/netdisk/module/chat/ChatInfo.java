package com.netdisk.module.chat;

import com.netdisk.service.impl.ChatServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(ChatServiceImpl.CHATINFO_COLLECTION)
public class ChatInfo {

    @Id
    String id;

    Long roomId;

    Long messageId;

    Long userId;

    Boolean system;

    Boolean saved;

    Boolean distributed;

    Boolean seen;

    Boolean deleted;

    Boolean failure;

    Boolean disableActions;

    Boolean disableReactions;
}
