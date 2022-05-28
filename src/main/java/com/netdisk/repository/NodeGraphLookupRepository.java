package com.netdisk.repository;

import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;

import java.util.List;
import java.util.Optional;

public interface NodeGraphLookupRepository {

    /**
     *
     * @param maxDepth 递归深度，0是只查询直接子节点
     * @return 返回符合条件的节点
     */
    List<FileNode> getSubTree(Long userId, Long nodeId, Long maxDepth);

    /**
     * 根据isFolder过滤结果
     */
    List<FileNode> getSubTree(Long userId, Long nodeId, Long maxDepth, boolean isFolder);

    /**
     * 查询share表
     * @param maxDepth 递归深度，0是只查询直接子节点
     * @return 返回符合条件的节点
     */
    List<Share> getShareSubTree(Long userId, Long nodeId, Long maxDepth);

}
