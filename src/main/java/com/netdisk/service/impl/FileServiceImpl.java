package com.netdisk.service.impl;

import com.netdisk.mapper.FileMapper;
import com.netdisk.pojo.File;
import com.netdisk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;

    @Override
    public int uploadFile(Map<String,Object> map) {
        return fileMapper.uploadFile(map);
    }

    @Override
    public List<File> queryByDir(Map<String, Object> map) {
        return fileMapper.queryFileByDir(map);
    }

    @Override
    public File queryFileByDirAndName(int dir_id, String file_name) {
        return fileMapper.queryFileByDirAndName(dir_id,file_name);
    }
}
