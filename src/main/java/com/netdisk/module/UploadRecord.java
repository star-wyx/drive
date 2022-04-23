package com.netdisk.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document("UploadRecord")
public class UploadRecord {

    @Id
    private String id;
    private Long userId;
    private String fileName;
    private String fileSize;
    private String uuid;
    private String status;
    private int complete;
    private String hash;
    private String fileType;
    private Boolean noErrorFlag;
    private Boolean isFinished;
    private Boolean isPause;
    private Boolean isGG;

}
