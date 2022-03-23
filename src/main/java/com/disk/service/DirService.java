package com.disk.service;

import com.disk.entity.Dir;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DirService {
    int NewFolder(Map<String,Object> map) throws IOException;
    List<Dir> querySubDir(Map<String,Object> map);
}
