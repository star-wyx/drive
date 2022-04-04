package com.netdisk.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document("sequence")
public class SeqInfo {
    @Id
    private String id;
    private String collName;
    private Long seqId;
}
