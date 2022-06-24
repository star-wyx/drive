package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.chat.LastMessage;
import com.netdisk.module.chat.RoomUser;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatParamDTO {

    @JsonProperty("_id")
    Long _id;

    @JsonProperty("roomId")
    Long roomId;

    @JsonProperty("files")
    Map files;

    @JsonProperty("dummyId")
    Long dummyId;

    @JsonProperty("seenList")
    List seenList;

    @JsonProperty("userId")
    Long userId;

    @JsonProperty("unreadCount")
    Long unreadCount;

    @JsonProperty("index")
    Long index;

    LastMessage lastMessage;

    List<RoomUser> userList;

    List<RoomUser> userAlreadyInRoomList;

    List<RoomUser> users;

    String newRoomName;

    String roomName;

    Long messageId;

    Map<String, List> reactions;

    Boolean isRemove;
}
