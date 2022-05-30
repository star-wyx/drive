package com.netdisk.task;

import com.netdisk.config.FileProperties;
import com.netdisk.util.FfmpegUtil;
import com.netdisk.util.UpdateInitial;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.xmlunit.builder.Input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

@Component
public class Bootstrap implements ApplicationRunner {

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FfmpegUtil ffmpegUtil;

    @Autowired
    UpdateInitial updateInitial;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File root = new File(fileProperties.getRootDir());
        File profileDir = new File(fileProperties.getProfileDir());
        File mp4Dir = new File(fileProperties.getMp4Dir());
        File tmpPath = new File(fileProperties.getTmpPath());

        if(!root.exists()){
            root.mkdirs();
        }
        if(!profileDir.exists()){
            profileDir.mkdirs();
        }
        if(!mp4Dir.exists()){
            mp4Dir.mkdirs();
        }
        if(!tmpPath.exists()){
            tmpPath.mkdirs();
        }

        ffmpegUtil.checkEncoders();

        if(fileProperties.getIsUpdate().equals("")){
            updateInitial.changeFileSystem();
            updateInitial.setMd5();
            updateInitial.setShared();
            updateInitial.setFolderSize();
            updateInitial.setUserIsShared();
            updateInitial.setShareRootNode();
            fileProperties.setIsUpdate("false");
        }

    }

}
