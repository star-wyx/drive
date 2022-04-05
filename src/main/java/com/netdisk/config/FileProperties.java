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
@PropertySource(value = "classpath:file.yml")
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private String rootDir;

    private Map<String, String> icon;

    private String otherIcon;
}