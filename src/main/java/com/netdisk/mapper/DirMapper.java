package com.netdisk.mapper;

import com.netdisk.pojo.Dir;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DirMapper {
    int NewFolder(Map<String,Object> map);
    List<Dir> querySubDir(Map<String,Object> map);
    Dir queryByDid(@Param("dir_id") int dir_id);
}
