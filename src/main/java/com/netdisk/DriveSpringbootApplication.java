package com.netdisk;

import com.netdisk.config.FileProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileProperties.class
})
public class DriveSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveSpringbootApplication.class, args);
    }

}

