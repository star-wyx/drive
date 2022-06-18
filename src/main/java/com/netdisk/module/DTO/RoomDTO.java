package com.netdisk.module.DTO;

import com.netdisk.module.chat.LastMessage;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomUser;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class RoomDTO {

    long roomId;

    String roomName;

    String avatar;

    /**
     * （需实时更新）房间针对该用户的未读信息
     */
    long unreadCount;

    /**
     * （需实时更新）房间该用户的排序
     */
    long index;

    /**
     * （需实时更新）房间的最后一条消息 其中为消息object
     */
    LastMessage lastMessage;

    //房间所有用户 其中为用户object
    List<RoomUser> users;

    //（需实时更新）正在打字的用户
//    List<Long> typingUsers;

}
