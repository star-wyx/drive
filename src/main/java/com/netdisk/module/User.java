package com.netdisk.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.service.impl.UserServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(UserServiceImpl.USER_COLLECTION)
public class User {
    private Long userId;
    private String userName;
    private String userPwd;
    private String userEmail;
    private String base64;
    private Long usedSize;
    private Long totalSize;
    private Boolean haveShared;
}
