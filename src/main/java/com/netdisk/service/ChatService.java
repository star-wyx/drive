package com.netdisk.service;

import com.netdisk.module.DTO.ChatParamDTO;
import com.netdisk.module.DTO.MessageDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.User;
import com.netdisk.module.chat.Message;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomUser;

import java.util.List;

public interface ChatService {

    void addRoom(List<Long> userList);

    Room getRoom(long roomId);

    MessageDTO addMessageToRoom(Long roomId, String content, byte[] files,
                                Long replyMessageId, List<Long> usersTag, Long senderId);

    long getNextMessageId(long roomId);

    List<RoomUser> getRoomUser(long roomId);

    ParamDTO fetchmessages(long userId, long roomId, long messageId);

    void changeDistributed(long userId, long roomId, long messageId, boolean bool);

    ChatParamDTO iSeen(long roomId, long messageId, long userId);

    ChatParamDTO changeSeenFrom(long roomId, long messageId, long userId);
}
