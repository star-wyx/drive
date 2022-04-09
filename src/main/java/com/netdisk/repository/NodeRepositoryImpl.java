package com.netdisk.repository;

import com.mongodb.client.MongoCollection;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.FileNode;
import com.netdisk.service.impl.FileServiceImpl;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.List;

public class NodeRepositoryImpl implements NodeGraphLookupRepository{

    private static final long MAX_DEPTH_SUPPORTED = 10000L;

    private final MongoTemplate mongoTemplate;

    public NodeRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<FileNode> getSubTree(Long userId, Long nodeId, Long maxDepth) {
        final Criteria byNodeId = new Criteria("nodeId").is(nodeId);
        final Criteria byTreeId = new Criteria("userId").is(userId);
        final MatchOperation matchStage = Aggregation.match(byTreeId.andOperator(byNodeId));

        GraphLookupOperation graphLookupOperation = GraphLookupOperation.builder()
                .from(FileServiceImpl.FILE_COLLECTION)
                .startWith("$nodeId")
                .connectFrom("nodeId")
                .connectTo("parentId")
                .restrict(new Criteria("userId").is(userId))
                .maxDepth(maxDepth != null ? maxDepth : MAX_DEPTH_SUPPORTED)
                .as("descendants");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, graphLookupOperation);

        return mongoTemplate.aggregate(aggregation, FileServiceImpl.FILE_COLLECTION, FileNode.class).getMappedResults();
    }

    @Override
    public List<FileNode> getSubTree(Long userId, Long nodeId, Long maxDepth, boolean isFolder) {
        final Criteria byNodeId = new Criteria("nodeId").is(nodeId);
        final Criteria byTreeId = new Criteria("userId").is(userId);
        final MatchOperation matchStage = Aggregation.match(byTreeId.andOperator(byNodeId));

        GraphLookupOperation graphLookupOperation = GraphLookupOperation.builder()
                .from(FileServiceImpl.FILE_COLLECTION)
                .startWith("$nodeId")
                .connectFrom("nodeId")
                .connectTo("parentId")
                .restrict(new Criteria("userId").is(userId))
                .maxDepth(maxDepth != null ? maxDepth : MAX_DEPTH_SUPPORTED)
                .restrict(Criteria.where("isFolder").is(true))
                .as("descendants");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, graphLookupOperation);

        return mongoTemplate.aggregate(aggregation, FileServiceImpl.FILE_COLLECTION, FileNode.class).getMappedResults();
    }
}
