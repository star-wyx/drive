package com.netdisk.module.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomUser {

    /**
     * userId
     */
    long _id;

    String username;

    String avatar;

    Status status;

}
