package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.FileNode;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class FileDTO {
    @JsonProperty("node_id")
    private Long nodeId;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("is_folder")
    private boolean isFolder;
    @JsonProperty("content_type")
    private String contentType;

    public FileDTO(FileNode fileNode){
        this.nodeId = fileNode.getNodeId();
        this.fileName = fileNode.getFileName();
        this.filePath = fileNode.getFilePath();
        this.isFolder = fileNode.isFolder();
        this.contentType = fileNode.getContentType();
    }
}
