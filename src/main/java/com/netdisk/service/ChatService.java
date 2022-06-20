package com.netdisk.service;

import com.netdisk.module.DTO.ChatParamDTO;
import com.netdisk.module.DTO.MessageDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.RoomDTO;
import com.netdisk.module.User;
import com.netdisk.module.chat.Message;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomInfo;
import com.netdisk.module.chat.RoomUser;

import java.util.List;

public interface ChatService {

    void addRoom(List<Long> userList, String roomName);

    Room getRoom(long roomId);

    List<RoomDTO> getAllRoom(long userId);

    MessageDTO addMessageToRoom(Long roomId, String content, byte[] files,
                                Long replyMessageId, List<Long> usersTag, Long senderId);

    long getNextMessageId(long roomId);

    List<RoomUser> getRoomUser(long roomId);

    ParamDTO fetchmessages(long userId, long roomId, long messageId);

    void changeDistributed(long userId, long roomId, long messageId, boolean bool);

    ChatParamDTO iSeen(long roomId, long messageId, long userId);

    ChatParamDTO changeSeenFrom(long roomId, long messageId, long userId);

    void updateRoomSeq(long roomId, long userId);

    long incRoomInfoUnread(long roomId, long userId);

    void clearRoomInfoUnread(long roomId, long userId);

    long getRoomInfoUnread(long roomId, long userId);

    public RoomDTO roomToDTO(Room room, RoomInfo roomInfo);

}
