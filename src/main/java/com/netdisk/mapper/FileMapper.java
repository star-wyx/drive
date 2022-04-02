package com.netdisk.mapper;

import com.netdisk.pojo.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileMapper {
    int uploadFile(Map<String, Object> map);
    List<File> queryFileByDir(Map<String,Object> map);
    File queryFileByDirAndName(@Param("dir_id") int dir_id, @Param("file_name") String file_name);
}
