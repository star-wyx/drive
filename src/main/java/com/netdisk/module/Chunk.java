package com.netdisk.module;

import com.netdisk.service.impl.ChunkServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@Document(ChunkServiceImpl.CHUNK_COLLECTION)
public class Chunk {

    @Id
    private String id;

    /**
     * 分片编号
     */
    private Long serialNo;

    private String uuid;

    private String md5;

    private Long userId;

    private Long nodeId;

    private String fileName;

    private String storePath;

    private Date uploadTime;

}
