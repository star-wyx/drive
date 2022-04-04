package com.netdisk.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class FilePropertiesTest {
    @Autowired
    FileProperties fileProperties;

    @Test
    public void propertiesTest(){
        System.out.println(fileProperties.getRootDir());
        System.out.println(fileProperties.getIcon());
        System.out.println(fileProperties.getOtherIcon());
    }

}