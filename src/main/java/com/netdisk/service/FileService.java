package com.netdisk.service;

import com.netdisk.pojo.File;

import java.util.List;
import java.util.Map;

public interface FileService {

    int uploadFile(Map<String,Object> map);
    List<File> queryByDir(Map<String,Object> map);
    File queryFileByDirAndName(int dir_id, String file_name);
}