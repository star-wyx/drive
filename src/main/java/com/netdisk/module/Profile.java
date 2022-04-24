package com.netdisk.module;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    private String userEmail;
    private String availableSize;
    private String filmSize;
    private String musicSize;
    private String picture;
    private String others;
//    private String base64;

    private List<String> ratio;

}
