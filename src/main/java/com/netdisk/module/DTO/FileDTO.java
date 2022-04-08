package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.FileNode;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileDTO {
    @JsonProperty("node_id")
    private Long nodeId;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("content_type")
    private String contentType;

    public FileDTO(FileNode fileNode){
        this.nodeId = fileNode.getNodeId();
        this.fileName = fileNode.getFileName();
        this.filePath = fileNode.getFilePath();
        this.contentType = fileNode.getContentType();
    }

    public static List<FileDTO> listConvert(List<FileNode> fileNodes){
        List<FileDTO> res = new ArrayList<>();
        for(FileNode fileNode:fileNodes){
            FileDTO fileDTO = new FileDTO(fileNode);
            res.add(fileDTO);
        }
        return res;
    }
}
