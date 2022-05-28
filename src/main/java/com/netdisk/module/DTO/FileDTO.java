package com.netdisk.module.DTO;

import ch.qos.logback.core.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.util.MyFileUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@NoArgsConstructor
public class FileDTO {
    @JsonProperty("node_id")
    private Long nodeId;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("is_Leaf")
    private Boolean isLeaf = null;
    @JsonProperty("base64")
    private String base64;
    @JsonProperty("isFavorite")
    private boolean isFavorite;
    private Long fileSize;
    private String fileSizeInUnit;
    private String uploadTime;
    @JsonProperty("isShared")
    private boolean isShared;

    public FileDTO(FileNode fileNode) {
        MyFileUtils myFileUtils = new MyFileUtils();
        this.nodeId = fileNode.getNodeId();
        this.fileName = fileNode.getFileName();
        this.filePath = fileNode.getFilePath();
        this.contentType = fileNode.getContentType();
        this.isFavorite = fileNode.isFavorites();
        if(fileNode.getBase64() != null){
            this.base64 = fileNode.getBase64();
        }
        this.fileSize = fileNode.getFileSize();
        this.uploadTime = fileNode.getUploadTime();
        this.isShared = fileNode.isShared();
//        this.fileSizeInUnit =  myFileUtils.getPrintSize(fileNode.getFileSize()); 无法调用myFileUtils
    }

    public static List<FileDTO> listConvert(List<FileNode> fileNodes) {
        List<FileDTO> res = new ArrayList<>();
        for (FileNode fileNode : fileNodes) {
            FileDTO fileDTO = new FileDTO(fileNode);
            res.add(fileDTO);
        }
        return res;
    }

}
