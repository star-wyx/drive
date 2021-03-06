package com.netdisk.module.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
     * 用户Id
     */
    @JsonProperty("new_userid")
    Long newUserId;


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
     * FileNode id
     */
    @JsonProperty("new_nodeId")
    Long newNodeId;

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

    /**
     * 是否分享FileNode
     */
    @JsonProperty("is_shared")
    Boolean isShared;

    /**
     * 文件md5值
     */
    @JsonProperty("hash")
    String md5;

    /**
     * 文件大小, 单位KB,MB,GB
     */
    @JsonProperty("size")
    String size;

    /**
     * 文件大小, 单位KB,MB,GB
     */
    @JsonProperty("fileSizeInUnit")
    String fileSizeInUnit;

    /**
     * 文件大小, 单位bit
     */
    @JsonProperty("fileSize")
    Long fileSize;

    /**
     * 文件类型
     */
    @JsonProperty("content_type")
    String contentType;

    /**
     * 上传是否成功
     */
    @JsonProperty("upload_success")
    Boolean uploadSuccess;

    /**
     * 上传任务建立成功
     */
    @JsonProperty("create_task")
    Boolean createTask;

    /**
     * UUID
     */
    @JsonProperty("uid")
    String uuId;

    /**
     * 分片编号
     */
    @JsonProperty("sliceNo")
    Long sliceNo;

    /**
     * 文件上传时间
     */
    @JsonProperty("upload_time")
    String uploadTime;

    /**
     * token
     */
    @JsonProperty("token")
    String token;

    /**
     * 旧密码
     */
    @JsonProperty("old_pwd")
    String oldPwd;

    /**
     * 新密码
     */
    @JsonProperty("new_pwd")
    String newPwd;

    /**
     * base 64
     */
    @JsonProperty("base64")
    String base64;

    /**
     * codec
     */
    @JsonProperty("codec")
    String codec;

    /**
     * 可用空间的比例
     */
    @JsonProperty("percentage")
    String percentage;

    /**
     * 文件list
     */
    @JsonProperty("file_list")
    List<Long> fileNodes;

    /**
     * 是否从所有分享接口进来
     */
    @JsonProperty("isAll")
    Boolean isAll;

    @JsonProperty("real_user_id")
    Long realUserId;

    @JsonProperty("share_files")
    List shareFileDTOS;

    @JsonProperty("MessageEnd")
    Boolean messageEnd;
}
