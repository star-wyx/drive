package com.netdisk.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyFileUtilsTest {

    @Autowired
    MyFileUtils myFileUtils;

    @Test
    public void testReadStream(){

        //顺便提一句 傻逼mac移动文件跟搬家一样难按:(
        File file = new File("/Users/star_wyx/Downloads/300MiB.bin");
        System.out.println(myFileUtils.getPartMd5ByStream(file));

    }

}