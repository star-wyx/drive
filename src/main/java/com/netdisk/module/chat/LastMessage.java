package com.netdisk.module.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastMessage {

    String content;

    long senderId;

    String userName;

    String timestamp;

    Boolean saved;

    Boolean distributed;

    Boolean seen;

    Boolean isNew;

}
