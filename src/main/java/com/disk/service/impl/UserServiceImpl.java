package com.disk.service.impl;

import com.disk.entity.User;
import com.disk.repository.UserRepository;
import com.disk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User queryUserNamePwd(Map<String, Object> map) {
        return userRepository.queryUserNamePwd(map);
    }

    @Override
    public int signIn(Map<String, Object> map) {
        User check = queryUserNamePwd(map);
        if (check == null) {
            return userRepository.signIn(map);
        }else{
            return 0;
        }
    }

    @Override
    public User queryUserByName(Map<String, Object> map) {
        return userRepository.queryUserByName(map);
    }

    @Override
    public User queryUserByEmail(Map<String, Object> map) {
        return userRepository.queryUserByEmail(map);
    }

    @Override
    public int insertUser(Map<String, Object> map) {
        return userRepository.insertUser(map);
    }

    @Override
    public List<String> queryDataByUserId(int userId) {
        return userRepository.queryDataByUserId(userId);
    }
}
