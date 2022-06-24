package com.netdisk.module;

import com.netdisk.service.impl.SharedServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(SharedServiceImpl.SHARED_COLLECTION)
public class Share {

    @Id
    private String id;
    private Long userId;
    private String userName;
    private Long nodeId;
    private String fileName;
    private String sharePath;
    private String storePath;
    private boolean isFolder;
    private List<Share> descendants;
    private Long parentId;
    private String contentType;

    private String md5;
    private String base64;
    private Long fileSize;
    private String uploadTime;



    public Share(FileNode fileNode, long parentId, String sharePath, String userName){
        this.userId = fileNode.getUserId();
        this.userName = userName;
        this.nodeId = fileNode.getNodeId();
        this.fileName = fileNode.getFileName();
        this.sharePath = sharePath;
        this.storePath = fileNode.getStorePath();
        this.isFolder = fileNode.isFolder();
        this.parentId = parentId;
        this.contentType = fileNode.getContentType();
        this.md5 = fileNode.getMd5();
        this.base64 = fileNode.getBase64();
        this.fileSize = fileNode.getFileSize();
        this.uploadTime = fileNode.getUploadTime();
    }
}
