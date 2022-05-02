package com.netdisk.config;

import com.netdisk.task.Bootstrap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@PropertySource(value = "classpath:codec.properties")
@ConfigurationProperties(prefix = "codec")
public class CodecProperties {

    private Map<String, Boolean> decoder;

    private Map<String, Boolean> encoder;

}
