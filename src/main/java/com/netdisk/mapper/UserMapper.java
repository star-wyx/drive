package com.netdisk.mapper;

import com.netdisk.pojo.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    public User queryUserNamePwd(Map<String, Object> map);
    public User queryUserEmailPwd(Map<String, Object> map);
    public User queryUserByName(Map<String, Object> map);
    public User queryUserByEmail(Map<String, Object> map);
    public int insertUser(Map<String,Object> map);
    public List<String> queryDataByUserId(int user_id);
}
