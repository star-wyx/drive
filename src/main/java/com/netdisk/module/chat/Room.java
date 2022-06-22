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
@Document(ChatServiceImpl.ROOM_COLLECTION)
public class Room {

    @Id
    String Id;

    long roomId;

    String roomName;

    String avatar;

    /**
     * （需实时更新）房间针对该用户的未读信息
     */
//    long unreadCount;

    /**
     * （需实时更新）房间该用户的排序
     */
//    long index;

    /**
     * （需实时更新）房间的最后一条消息 其中为消息object
     */
    Long lastMessageId;

    //房间所有用户 其中为用户object
    List<Long> userList;

    //（需实时更新）正在打字的用户
    List<Long> typingUsers;

    Long nextMessageId;
}

