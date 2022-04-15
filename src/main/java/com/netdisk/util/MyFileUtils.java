package com.netdisk.util;

import com.netdisk.config.FileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public final class MyFileUtils {

    @Autowired
    FileProperties fileProperties;

    public void createFolder(String dictionary) {
        Path path = Paths.get(fileProperties.getRootDir() + dictionary);
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFile(MultipartFile file, String dictionary) {
        File localFile = new File(fileProperties.getRootDir() + dictionary);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void icon(String contentType) {
        /**
         * folder-fill
         * filetype-xxx
         * file-earmark
         * film
         * music-note-beamed
         * file-earmark-zip-fill
         * file-earmark-arrow-down bt种子
         * image
         * .svg
         */
    }

    public static String getMD5(String str) {
        String slat = "&%5123***&&%%$$#@";
        String base = str + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
