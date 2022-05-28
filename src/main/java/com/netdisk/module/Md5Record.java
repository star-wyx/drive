package com.netdisk.module;

import com.netdisk.service.impl.Md5ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(Md5ServiceImpl.Md5_COLLECTION)
public class Md5Record {

    @Id
    private String id;

    private String md5;

    private long index;

}
