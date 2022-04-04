package com.netdisk.repository;

import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.FileNode;

import java.util.List;
import java.util.Optional;

public interface NodeGraphLookupRepository {

    /**
     *
     * @param maxDepth 递归深度，0是只查询直接子节点
     * @return 返回符合条件的节点
     */
    List<FileNode> getSubTree(Long userId, Long nodeId, Long maxDepth);

}
