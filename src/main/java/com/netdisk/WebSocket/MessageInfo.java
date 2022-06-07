package com.netdisk.WebSocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

/**
 * 接收前台用户信息类
 *
 * @author liangxifeng 2018-07-07
 */
@Component
@Data
@ToString
public class MessageInfo {

    @JsonProperty("user_id")
    String user_id;

}