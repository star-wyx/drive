package com.netdisk.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document("Token")
public class Token {

    @Id
    private String id;
    private String userName;
    private String activeTime;

}
