package com.disk.repository;

import com.disk.entity.File;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FileRepository {
    int uploadFile(Map<String, Object> map);
    List<File> queryFileByDir(Map<String,Object> map);
    File queryFileByDirAndName(@Param("dir_id") int dir_id, @Param("file_name") String file_name);
}
