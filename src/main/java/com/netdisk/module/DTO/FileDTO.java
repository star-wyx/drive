package com.netdisk.module.DTO;

import ch.qos.logback.core.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.config.FileProperties;
import com.netdisk.module.FileNode;
import com.netdisk.util.MyFileUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
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

    public FileDTO(FileNode fileNode) {
        this.nodeId = fileNode.getNodeId();
        this.fileName = fileNode.getFileName();
        this.filePath = fileNode.getFilePath();
        this.contentType = fileNode.getContentType();
    }

    public static List<FileDTO> listConvert(List<FileNode> fileNodes) {
        List<FileDTO> res = new ArrayList<>();
        for (FileNode fileNode : fileNodes) {
            FileDTO fileDTO = new FileDTO(fileNode);
            res.add(fileDTO);
        }
        return res;
    }

    @Override
    public int compareTo(FileDTO o) {
        int rank = ContentTypeRank(this.getContentType(), o.getContentType());
        if (rank != 0) {
            return rank;
        }
        return this.getFileName().compareTo(o.getFileName());
    }

    public int ContentTypeRank(String type1, String type2) {
        Map<String, Integer> map = new HashMap<>();
        map.put("folder-fill", 5);
        map.put("film", 4);
        map.put("music-note-beamed", 3);
        map.put("image", 2);
        map.put("file-earmark", 0);

        return map.getOrDefault(type1,1) - map.getOrDefault(type2,1);

    }
}
