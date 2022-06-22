package com.netdisk.service;

import com.netdisk.module.DTO.ChatParamDTO;
import com.netdisk.module.DTO.MessageDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.RoomDTO;
import com.netdisk.module.User;
import com.netdisk.module.chat.*;

import java.util.List;

public interface ChatService {

    Room addRoom(List<Long> userList, String roomName);

    Room getRoom(long roomId);

    List<RoomDTO> getAllRoom(long userId);

    MessageDTO addMessageToRoom(Long roomId, String content, byte[] files,
                                Long replyMessageId, List<Long> usersTag, Long senderId);

    long getNextMessageId(long roomId);

    List<RoomUser> getRoomUser(long roomId, boolean needStatus);

    ParamDTO fetchmessages(long userId, long roomId, long messageId);

    void changeDistributed(long userId, long roomId, long messageId, boolean bool);

    ChatParamDTO iSeen(long roomId, long messageId, long userId);

    ChatParamDTO changeSeenFrom(long roomId, long messageId, long userId);

    void updateRoomSeq(long roomId, long userId);

    long incRoomInfoUnread(long roomId, long userId);

    void clearRoomInfoUnread(long roomId, long userId);

    long getRoomInfoUnread(long roomId, long userId);

    RoomDTO roomToDTO(Room room, RoomInfo roomInfo, long userId, long unread);

    ChatParamDTO getUnreadDTO(long roomId, long unread, long userId);

    LastMessage getLastMessage(long roomId, long lastId, long userId, long unread);

    ChatParamDTO splitRoomUser(Long roomId, Long userId);

    List<Long> deleteRoom(long roomId, long userId);

    List<Long> removeUserFromUser(long roomId, long userId);

    MessageDTO sendSysMessage(Long roomId, String content);

    Long addUserToRoom(long roomId, List<Long> newUserList, List<Long> addedUser);

    RoomDTO getRoomDTO(long userId, long roomId);

    String welcoming(List<Long> userList);

    String newRoomName(List<Long> userList);
}
