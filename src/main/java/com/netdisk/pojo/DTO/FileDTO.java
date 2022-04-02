package com.netdisk.pojo.DTO;

import com.netdisk.pojo.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class FileDTO {
    @JsonProperty("file_id")
    private int id;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("upload_time")
    private Timestamp uploadTime;

    public FileDTO(File file){
        this.id = file.getId();
        this.fileName = file.getFileName();
        this.filePath = file.getFilePath();
        this.uploadTime = file.getUploadTime();
    }
}
