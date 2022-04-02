package com.netdisk.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class File {
    private int id;
    private String fileName;
    private String filePath;
    private String storePath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp uploadTime; //todo
    private int uid;
    private int dirId;
}
