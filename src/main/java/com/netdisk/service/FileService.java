package com.netdisk.service;

import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.util.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    /**
     * 返回浏览路径的每一层文件夹id
     */
    List queryBrowsePath(User user, List content);

    /**
     * 上传文件
     * user, nodeId, fileList
     */
    int uploadFile(User user, Long nodeId,MultipartFile[] list);

    List<FileNode> sub(Long userId, Long nodeId, Long maxDepth);

    /**
     * 新建目录并记录在数据库
     * user, nodeId, FileName
     */
    boolean createDir(User user, Long nodeId, String fileName);

    /**
     * 为新用户新建本地存储目录并记录在数据库
     */
    void createUserFile(User user);

    /**
     * 查看某一文件夹下的所有目录及文件
     * user, nodeId
     */
    List queryFolderContent(User user, Long nodeId);

    /**
     * 用户登陆时返回根目录内容
     * @param user
     * @return
     */
    Response queryFolderRootContent(User user);

    /**
     * 查找文件信息
     * userId, nodeId
     */
    FileNode queryFolderById(Long userId,Long nodeId);

    /**
     * 根据用户id，父目录id，文件名称查找Folder
     */
    FileNode queryFolderByNameId(Long userId, Long parentId, String fileName);


    /**
     * 根据用户id，父目录id，文件名称查找File
     */
    FileNode queryFileByNameId(Long userId, Long parentId, String fileName);

    /**
     * 根据userId,contentType,isFavorites,isFolder检索文件
     */
    ParamDTO queryAll(Long userId, String contentType);

    /**
     * 收藏或取消收藏文件夹
     */
    int favoriteFile(Long userId,Long nodeId,Boolean isFavorites);

    /**
     * 查找已收藏的FileNode
     */
    ParamDTO queryFavorites(Long userId);

    /**
     * 查找目录下的所有文件夹
     */
    ParamDTO queryAllFolder(Long userId, Long nodeId);
}
