package com.netdisk.module;

import com.netdisk.service.impl.ChunkServiceImpl;
import com.netdisk.service.impl.Mp4ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@Document(Mp4ServiceImpl.Mp4_COLLECTION)
public class Mp4 {

    @Id
    private String id;

    private String fileName;

    private String md5;

    private String storePath;

    private String otherMd5;

    private String status;

    private Date uploadTime;

    private Long userId;


}
