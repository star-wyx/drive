package com.netdisk.service;

import com.netdisk.module.Chunk;
import com.netdisk.module.DTO.ParamDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ChunkService {

    /**
     * 新建chunk, 插入数据库并保存分片
     */
    Long createTask(String md5, String uuid,Long userId, Long nodeId, String fileName);

    /**
     * 存储分片
     */
    int uploadSlice(MultipartFile file, String uuid, String sliceName);

    /**
     * merge
     */
    int merge(String uuid, String md5);

    /**
     * 删除数据库chunk
     */
    Long deleteChunk(String uuid);

    /**
     * 删除缓存文件
     */
    int abort(String uuid, String md5);
}
