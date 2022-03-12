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
    public User queryUser(Map<String, Object> map) {
        return userRepository.queryUser(map);
    }

}
