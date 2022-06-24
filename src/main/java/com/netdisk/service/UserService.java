package com.netdisk.service;

import com.netdisk.module.DTO.UserDTO;
import com.netdisk.module.User;
import com.netdisk.util.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {

    Response login(String userName, String pwd);

    /**
     * 添加用户
     * userName, userEmail, userPwd
     */
    Response add(UserDTO userDTO);

    /**
     * 根据用户名查询用户
     * user或userName
     */
    User getUserByName(String userName);

    /**
     * 根据邮箱查询用户
     * user或userEmail
     */
    User getUserByEmail(String userEmail);

    /**
     * 根据用户id查询用户
     */
    User getUserById(Long userId);

    /**
     * 上传头像
     */
    boolean uploadPicture(MultipartFile file, Long userId);

    /**
     * 上传Room头像
     */
    boolean uploadRoomPicture(MultipartFile file, Long roomId);

    /**
     * 修改密码
     */
    int updatePwd(Long userId, String newPwd, String oldPwd);

    /**
     * 修改用户空间
     */
    boolean updateSize(Long userId, Long fileSize);

    /**
     * 查看可用空间
     */
    Long availableSpace(Long userId);


    /**
     * 设置可用空间
     */
    void setAvailableSpace(Long userId, Long remain);

    /**
     * 更改用户分享状态
     */
    void setHaveShared(long userId, boolean isShared);

    /**
     * 返回所有分享文件的用户
     */
    List querySharedUser(long querySharedUser);

    /**
     * 在main聊天室中添加用户
     */
    void addUserToMainRoom(long userId);
}
