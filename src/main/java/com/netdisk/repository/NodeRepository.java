package com.netdisk.repository;

import com.netdisk.module.FileNode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends MongoRepository<FileNode,Object>, NodeGraphLookupRepository {

    /**
     *
     * @param userId 用户名
     * @return 返回用户名下的所有文件节点
     */
    Optional<List<FileNode>> findDistinctByUserId(Long userId);


    /**
     *
     * @param userId 用户名
     * @param nodeId 节点id
     * @return 查找用户名下的符合id的节点
     */
    Optional<FileNode> findDistinctByUserIdAndNodeId(Long userId, Long nodeId);
}
