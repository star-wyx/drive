package com.disk.service;

import com.disk.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    public List<User> findAll();
    public User queryUser(Map<String,Object> map);
}
