package com.disk.repository;

import com.disk.entity.Dir;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DirRepository {
    int NewFolder(Map<String,Object> map);
    List<Dir> querySubDir(Map<String,Object> map);
    Dir queryByDid(@Param("dir_id") int dir_id);
}
