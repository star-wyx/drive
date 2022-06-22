package com.netdisk.module.chat;

import com.netdisk.service.impl.ChatServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(ChatServiceImpl.MESSAGE_COLLECTION)
public class Message {

    @Id
    String id;

    long messageId;

    long roomId;

    long senderId;

    String content;

    String userName;

    String avatar;

    String date;

    String timestamp;

    Long replayMessage;

    MessageFile messageFile;

    Reactions reactions;

    List<Long> usersTag;


}
