package com.disk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Dir {
    @JsonProperty("dir_id")
    private int dirId;
    @JsonProperty("dir_name")
    private String dirName;
    @JsonProperty("parent_id")
    private int parentId;
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("dir_path")
    private String dirPath;
    @JsonProperty("store_path")
    private String storePath;
}
