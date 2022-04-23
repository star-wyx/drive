package com.netdisk.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserService userService;

    @Autowired
    FileService fileService;

    @Test
    public void testVerify(String token) {
        if (JwtUtil.verify(token, "tom", "WWww123456,./")) {
            System.out.println("success");
        } else {
            System.out.println("fail");
        }
    }

    @Test
    public void testLogin() {
        Response res = userService.login("tom", "WWww123456,./");
        User user = (User) res.getData();
        String token = JwtUtil.sign(user.getUserName(), user.getUserPwd());

        DecodedJWT jwt = JWT.decode(token);
        System.out.println(jwt.getClaim("userName")
                .asString());

        testVerify(token);
    }
}