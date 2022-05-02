package com.netdisk.config;

import com.netdisk.task.Bootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodecPropertiesTest {

    @Autowired
    CodecProperties codecProperties;

    @Test
    public void codeTest(){

        codecProperties.getEncoder().put("h264_qsv",true);
        System.out.println(codecProperties.getEncoder());
        Map<String, Boolean> encoder = codecProperties.getEncoder();
        Map<String, Boolean> decoder = codecProperties.getDecoder();
        System.out.println(encoder);
        System.out.println(decoder);
    }

}