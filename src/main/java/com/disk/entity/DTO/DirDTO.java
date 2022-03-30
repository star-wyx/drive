package com.disk.entity.DTO;

import com.disk.entity.Dir;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DirDTO {
    @JsonProperty("dir_id")
    private int dirId;
    @JsonProperty("dir_name")
    private String dirName;
    @JsonProperty("dir_path")
    private String dirPath;

    public DirDTO(Dir dir){
        this.dirId = dir.getDirId();
        this.dirName = dir.getDirName();
        this.dirPath = dir.getDirPath();
    }
}
