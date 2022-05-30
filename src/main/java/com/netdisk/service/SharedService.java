package com.netdisk.service;

import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.module.User;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public interface SharedService {

    /**
     * 为新用户创建分享跟目录
     */
    void createUserRoot(long userId, String userName);

    /**
     * 分享文件
     */
    int shareFile(Long userId, Long nodeId);

    /**
     * 分享文件夹
     */
    void shareFolder(FileNode folder, Share shareFolder, String userName);

    /**
     * 取消分享文件
     */
    int cancelShare(Long userId, Long nodeId);

    /**
     * 转存他人分享的文件
     */
    int saveSharedFile(long userId, User newUser, FileNode shareNode, Long folderId);

    /**
     * 转存他人分享的文件夹
     */
    int saveSharedFolder(long userId, User newUser, FileNode fileNode, Long folderId);

    /**
     * 查看分享的文件
     */
    List querySharedFile(long userId, long nodeId,  boolean isAll);

    /**
     * 查看分享的contentType
     */
    ParamDTO queryAll(long userId, long realUserId, String contentType);

    /**
     * 返回piazza
     */
    List queryPiazza(long realUserId);

    /**
     * 返回分享路径浏览路径的每一层文件夹id
     */
    List queryBrowsePath(List<String> content);

    List queryBrowsePathAll(List<String> content);

    List queryBrowsePathOne(List<String> content);

    /**
     * 如果fileNode更改filePath, update share表
     */
    void updateMoveFile(FileNode newParent, FileNode fileNode);

    /**
     * 如果fileNode更改fileName, update share表
     */
    void updateRename(long userId, long nodeId, String newName);

    /**
     * 根据sharePath返回用户名
     */
    String getUserName(Share share);

    /**
     * 根据sharePath返回fileName
     */
    String getFileName(Share share);

    /**
     * 修改子文件sharePath
     */
    void updateSharePath(Share share, List<Share> list);

    /**
     * 更新nodeId及其父文件夹的大小，从nodeId开始
     */
    void updateShareFolderSize(long userId, long nodeId, long fileSize);

}
