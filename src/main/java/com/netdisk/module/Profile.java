package com.netdisk.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    private Long userId;
    private String userEmail;
    private String availableSize;
    private String filmSize;
    private String musicSize;
    private String torrentSize;
    private String picture;
    private String others;
    private String base64;

}
