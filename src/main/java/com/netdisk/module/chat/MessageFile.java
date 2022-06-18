package com.netdisk.module.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageFile {

    String name;

    Long size;

    String type;

    String url;

    String preview;

}
