package com.disk.repository;

import com.disk.entity.User;

import java.util.List;
import java.util.Map;

public interface UserRepository {
    public User queryUserNamePwd(Map<String, Object> map);
    public User queryUserEmailPwd(Map<String, Object> map);
    public User queryUserByName(Map<String, Object> map);
    public User queryUserByEmail(Map<String, Object> map);
    public int insertUser(Map<String,Object> map);
    public List<String> queryDataByUserId(int user_id);
}
