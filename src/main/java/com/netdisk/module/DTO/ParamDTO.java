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


    /**
     * 在登陆时用content来返回用户根目录内容
     *
     */
    List content;

    /**
     * FileNode id
     */
    @JsonProperty("node_id")
    Long nodeId;

    /**
     * 文件或文件夹的网盘路径
     */
    @JsonProperty("file_path")
    String filePath;

    /**
     * 返回资源的数量
     */
    @JsonProperty("content_size")
    Integer contentSize;


    /**
     * 是否收藏FileNode
     */
    @JsonProperty("is_favorites")
    Boolean isFavorites;
}
