package com.netdisk.service.impl;

import com.netdisk.mapper.UserMapper;
import com.netdisk.pojo.User;
import com.netdisk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User queryUserNamePwd(Map<String, Object> map) {
        return userMapper.queryUserNamePwd(map);
    }

    @Override
    public User queryUserEmailPwd(Map<String, Object> map) {
        return userMapper.queryUserEmailPwd(map);
    }

    @Override
    public User queryUserByName(Map<String, Object> map) {
        return userMapper.queryUserByName(map);
    }

    @Override
    public User queryUserByEmail(Map<String, Object> map) {
        return userMapper.queryUserByEmail(map);
    }

    @Override
    public int insertUser(Map<String, Object> map) {
        return userMapper.insertUser(map);
    }

    @Override
    public List<String> queryDataByUserId(int userId) {
        return userMapper.queryDataByUserId(userId);
    }
}
