package com.netdisk.service;

import com.netdisk.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    public User queryUserNamePwd(Map<String,Object> map);
    public User queryUserEmailPwd(Map<String,Object> map);
    public User queryUserByName(Map<String,Object> map);
    public User queryUserByEmail(Map<String,Object> map);
    public int insertUser(Map<String,Object> map);
    public List<String> queryDataByUserId(int userId);
}
