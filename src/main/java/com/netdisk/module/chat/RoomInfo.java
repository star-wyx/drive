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
@Document(ChatServiceImpl.ROOMINFO_COLLECTION)
public class RoomInfo {

    @Id
    private String id;

    private Long roomId;

    private Long userId;

    private Long unread;

    private Long index;

}
