package com.netdisk.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@PropertySource(value = "classpath:file.properties")
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private String rootDir;

    private Map<String, String> icon;

    private String otherIcon;

    public String tmpPath;

    public String profileDir;

    public String mp4Dir;

    public String separator;

    public int sliceSizeMB;

    private Long defaultSpace;

    private String ffmpegPath;

    private String ffprobePath;

    private String isUpdate;

    private String avatarPath;
}
