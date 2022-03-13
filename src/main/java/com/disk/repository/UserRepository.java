package com.disk.repository;

import com.disk.entity.User;

import java.util.List;
import java.util.Map;

public interface UserRepository {
    public List<User> findAll();
    public User queryUserNamePwd(Map<String, Object> map);
    public int signIn(Map<String,Object> map);
    public User queryUserByName(Map<String, Object> map);
    public User queryUserByEmail(Map<String, Object> map);
    public int insertUser(Map<String,Object> map);
    public List<String> queryDataByUserId(int user_id);
}
