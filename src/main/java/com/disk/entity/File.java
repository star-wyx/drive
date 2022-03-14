package com.disk.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class File {
    private int id;
    private String fileName;
    private Timestamp uploadTime;
    private int uid;
}
