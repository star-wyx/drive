package com.netdisk.service;

import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.util.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FileService {

    /**
     * 上传文件
     * userName, nodeId
     */
    int uploadFile(String userName, Long nodeId,MultipartFile[] list);

    List<FileNode> sub(Long userId, Long nodeId, Long maxDepth);

    /**
     * 新建目录并记录在数据库
     * userName, nodeId, FileName
     */
    boolean createDir(ParamDTO paramDTO);

    /**
     * 为新用户新建本地存储目录并记录在数据库
     */
    void createUserFile(User user);

    /**
     * 查看某一文件夹下的所有目录及文件
     * userName, nodeId
     */
    List queryFolderContent(ParamDTO paramDTO);

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
}
