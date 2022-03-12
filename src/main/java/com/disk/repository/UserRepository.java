package com.disk.repository;

import com.disk.entity.User;

import java.util.List;
import java.util.Map;

public interface UserRepository {
    public List<User> findAll();
    public User queryUser(Map<String, Object> map);
}
