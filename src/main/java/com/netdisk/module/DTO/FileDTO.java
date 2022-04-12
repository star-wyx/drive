package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.FileNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FileDTO implements Comparable<FileDTO> {
    @JsonProperty("node_id")
    private Long nodeId;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("is_Leaf")
    private boolean isLeaf;

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

    @Override
    public int compareTo(FileDTO o) {
        int contentCompare = this.contentType.compareTo(o.getContentType());
        if(contentCompare != 0){
            return contentCompare;
        }else{
            return this.fileName.compareTo(o.getFileName());
        }
    }
}
