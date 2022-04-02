package com.netdisk.service;

import com.netdisk.pojo.Dir;
import com.netdisk.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DirService {
    int NewFolder(Map<String,Object> map) throws IOException;
    int NewFolderUser(User user) throws IOException;
    Dir queryDir(@Param("dir_id") int dir_id);
    List<Dir> querySubDir(Map<String,Object> map);
}