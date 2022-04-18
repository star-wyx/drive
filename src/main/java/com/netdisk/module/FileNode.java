package com.netdisk.module;

import com.netdisk.service.impl.FileServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
@Document(FileServiceImpl.FILE_COLLECTION)
public class FileNode implements Comparable<FileNode> {

    @Id
    private String id;
    private Long userId;
    private Long nodeId;
    private String fileName;
    private String filePath;
    private String storePath;
    private boolean isFolder;
    private List<FileNode> descendants;
    private Long parentId;
    private String contentType;
    private boolean isFavorites;
    private String md5;
    private String base64;
    private String fileSize;
    private String uploadTime;



    @Override
    public int compareTo(FileNode fileNode){
        if(this.nodeId > fileNode.getNodeId()){
            return 1;
        }else{
            return -1;
        }
    }
}
