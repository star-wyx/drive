package com.disk.repository;

import com.disk.entity.File;

import java.util.List;
import java.util.Map;

public interface FileRepository {
    int uploadFile(Map<String, Object> map);
    List<File> queryFileByDir(Map<String,Object> map);
}
