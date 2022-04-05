package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ParamDTO {
    /**
     * 文件或文件夹名
     */
    @JsonProperty("file_name")
    String filename;

    /**
     * 当前网盘路径
     */
    @JsonProperty("current_dic")
    String currentDictionary;

    /**
     * 是否是文件夹
     */
    @JsonProperty("is_folder")
    Boolean isFolder;

    /**
     * 用户名
     */
    @JsonProperty("user_name")
    String userName;

    /**
     * 用户Id
     */
    @JsonProperty("user_id")
    Long userId;

    List content;

    /**
     * FileNode id
     */
    @JsonProperty("node_id")
    Long nodeId;
}
