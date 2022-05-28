package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.util.MyFileUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@Component
@NoArgsConstructor
public class ShareFileDTO {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("node_id")
    private Long nodeId;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("share_path")
    private String sharePath;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("base64")
    private String base64;
    private Long fileSize;
    private String fileSizeInUnit;
    private String uploadTime;

    public ShareFileDTO(Share share, String fileSizeInUnit) {
        this.userId = share.getUserId();
        this.userName = share.getUserName();
        this.nodeId = share.getNodeId();
        this.fileName = share.getFileName();
        this.sharePath = share.getSharePath();
        this.contentType = share.getContentType();
        if (share.getBase64() != null) {
            this.base64 = share.getBase64();
        }
        this.fileSize = share.getFileSize();
        this.fileSizeInUnit = fileSizeInUnit;
        this.uploadTime = share.getUploadTime();
    }
}
