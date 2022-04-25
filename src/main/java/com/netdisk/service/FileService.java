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
    int uploadFile(User user, Long nodeId, MultipartFile[] list);

    /**
     * 返回当前文件夹可用的文件名
     */

    String availableFoldereName(Long userId, Long nodeId, String fileName);


    /**
     * 数据库中记录新文件
     * user, nodeId, fileList
     */
    void insertFileNode(User user, Long nodeId, String fileName, String md5, long size);

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
    List<List> queryFolderContent(User user, Long nodeId);

    /**
     * 查看文件路径是否存在
     * filePath
     */
    Long checkFilePath(Long userId, String filePath);

    /**
     * 用户登陆时返回根目录内容
     *
     * @param user
     * @return
     */
    ParamDTO queryFolderRootContent(User user);

    /**
     * 查找文件信息
     * userId, nodeId
     */
    FileNode queryFolderById(Long userId, Long nodeId);

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
    int favoriteFile(Long userId, Long nodeId, Boolean isFavorites);

    /**
     * 查找已收藏的FileNode
     */
    ParamDTO queryFavorites(Long userId);

    /**
     * 查找目录下的所有文件夹
     */
    ParamDTO queryAllFolder(User user, Long nodeId);

    /**
     * 检查md5值是否存在
     */
    FileNode checkMd5(String md5);

    /**
     * 移动文件夹
     */
    boolean moveFile(Long userId, Long newNodeId, Long oldNodeId);

    /**
     * 改变子节点路径
     */
    void chChildPath(FileNode fileNode);

    /**
     * 删除文件
     */
    Long deleteFile(Long userId, Long nodeId);

    /**
     * 是否是图像
     */
    boolean isImage(String contentType);


    /**
     * 获取当前时间
     */
    String getTime();

    /**
     * 获取文件详细信息
     */
    ParamDTO getDetail(Long userId, Long nodeId);

    /**
     * 获取用户下的所有文件
     */
    List<FileNode> queryAllFiles(Long userId);
}
