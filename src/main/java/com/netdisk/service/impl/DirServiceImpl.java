package com.netdisk.service.impl;

import com.netdisk.mapper.DirMapper;
import com.netdisk.pojo.Dir;
import com.netdisk.pojo.User;
import com.netdisk.service.DirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DirServiceImpl implements DirService {

    @Autowired
    private DirMapper dirMapper;

    @Override
    public int NewFolder(Map<String, Object> map) throws IOException {
        Dir parent = dirMapper.queryByDid((int) map.get("parent_id"));
        String dir_path = parent.getDirPath()+"/"+map.get("dir_name");
        String store_path = parent.getStorePath() + "/" +map.get("dir_name");
        map.put("dir_path",dir_path);
        map.put("store_path",store_path);
        Path path = Paths.get(store_path);
        Path pathCreate = Files.createDirectories(path);

        return dirMapper.NewFolder(map);
    }

    @Override
    public int NewFolderUser(User user) throws IOException {
//        #{dir_name},#{parent_id},#{user_id},#{dir_path},#{store_path}
        Map<String, Object> map = new HashMap<>();
        Dir parent = dirMapper.queryByDid(1);
        map.put("dir_name", user.getUserName());
        map.put("parent_id", 1);
        map.put("user_id", user.getUserId());
        map.put("dir_path","/"+user.getUserName());
        String store_path = parent.getStorePath()+"/"+user.getUserName();
        map.put("store_path", store_path);
        Path path = Paths.get(store_path);
        Path pathCreate = Files.createDirectories(path);
        return dirMapper.NewFolder(map);
    }

    @Override
    public Dir queryDir(int dir_id) {
        return dirMapper.queryByDid(dir_id);
    }

    @Override
    public List<Dir> querySubDir(Map<String, Object> map) {
        return dirMapper.querySubDir(map);
    }
}
