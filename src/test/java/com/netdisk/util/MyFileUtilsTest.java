package com.netdisk.util;

import com.netdisk.config.FileProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyFileUtilsTest {

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    FileProperties fileProperties;

    @Test
    public void testmkv(){
        File srcFile = new File(fileProperties.getRootDir() + "/tom/Restaurant to Another World S02E11.mkv");
        File desFile = new File(fileProperties.getMp4Dir() + "/tom/Restaurant to Another World S02E11.mp4");
        try {
            MyFileUtils.convertToMp4(srcFile,desFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}