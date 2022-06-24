package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.ShareFileDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.module.User;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.Md5Service;
import com.netdisk.service.SeqService;
import com.netdisk.service.SharedService;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.TypeComparator;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SharedServiceImpl implements SharedService {

    public static final String SHARED_COLLECTION = "shared";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SeqService seqService;

    @Autowired
    Md5Service md5Service;

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    NodeRepository nodeRepository;

    @Autowired
    TypeComparator typeComparator;

    @Autowired
    FileProperties fileProperties;

    @Override
    public void createUserRoot(long userId, String userName) {
        Share share = new Share(null,
                userId,
                userName,
                1L,
                userName,
                "/" + userName,
                "/" + userName,
                true,
                null,
                0L,
                fileProperties.getIcon().get("folder"),
                null,
                null,
                0L,
                myFileUtils.getTime()
        );
        mongoTemplate.save(share, SHARED_COLLECTION);
    }

    @Override
    public int shareFile(Long userId, Long nodeId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        Update update = new Update();
        update.set("isShared", true);
        mongoTemplate.updateFirst(query, update, FileServiceImpl.FILE_COLLECTION);

        Query userQuery = new Query();
        userQuery.addCriteria(Criteria.where("userId").is(userId));
        User user = mongoTemplate.findOne(userQuery, User.class, UserServiceImpl.USER_COLLECTION);

        Share share = new Share();
        if (fileNode.getParentId() == 1) {
            share = new Share(fileNode, fileNode.getParentId(), fileNode.getFilePath(), user.getUserName());
        } else {
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(userId));
            q.addCriteria(Criteria.where("nodeId").is(fileNode.getParentId()));
            FileNode folder = mongoTemplate.findOne(q, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            if (folder.isShared()) {
                Query shareFolderQuery = new Query();
                shareFolderQuery.addCriteria(Criteria.where("userId").is(folder.getUserId()));
                shareFolderQuery.addCriteria(Criteria.where("nodeId").is(folder.getNodeId()));
                Share shareFolder = mongoTemplate.findOne(shareFolderQuery, Share.class, SHARED_COLLECTION);
                share = new Share(fileNode, fileNode.getParentId(), shareFolder.getSharePath() + "/" + fileNode.getFileName(), user.getUserName());
            } else {
                share = new Share(fileNode, 1L, "/" + user.getUserName() + "/" + fileNode.getFileName(), user.getUserName());
            }
        }
        mongoTemplate.save(share);

        if (fileNode.isFolder()) {
            shareFolder(fileNode, share, user.getUserName());
        }
        updateShareFolderSize(share.getUserId(), share.getParentId(), share.getFileSize());
        return 0;
    }

    @Override
    public void shareFolder(FileNode folder, Share shareFolder, String userName) {
        List<FileNode> list = nodeRepository.getSubTree(folder.getUserId(), folder.getNodeId(), 0L).get(0).getDescendants();
        Update update = new Update();
        update.set("isShared", true);
        for (FileNode f : list) {
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(f.getUserId()));
            q.addCriteria(Criteria.where("nodeId").is(f.getNodeId()));
            if (f.isShared()) {
                mongoTemplate.remove(q, Share.class, SHARED_COLLECTION);
            } else {
                mongoTemplate.updateFirst(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            }

            Share tmp = new Share(f, f.getParentId(), shareFolder.getSharePath() + "/" + f.getFileName(), userName);
            mongoTemplate.save(tmp);

            if (f.isFolder()) {
                shareFolder(f, tmp, userName);
            }
        }
    }

    @Override
    public int cancelShare(Long userId, Long nodeId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        if (!fileNode.isShared()) {
            return 0;
        }
        Update update = new Update();
        update.set("isShared", false);
        mongoTemplate.updateFirst(query, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        Share share = mongoTemplate.findOne(query, Share.class, SHARED_COLLECTION);
        updateShareFolderSize(share.getUserId(), share.getParentId(), -share.getFileSize());

        if (!fileNode.isFolder()) {
            mongoTemplate.updateFirst(query, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        } else {
            List<FileNode> list = nodeRepository.getSubTree(userId, nodeId, null).get(0).getDescendants();
            for (FileNode f : list) {
                Query q = new Query();
                q.addCriteria(Criteria.where("userId").is(userId));
                q.addCriteria(Criteria.where("nodeId").is(f.getNodeId()));
                mongoTemplate.updateFirst(q, update, FileNode.class, FileServiceImpl.FILE_COLLECTION);

                mongoTemplate.remove(q, Share.class, SHARED_COLLECTION);
            }
        }
        mongoTemplate.remove(query, Share.class, SHARED_COLLECTION);
        return 0;
    }

    @Override
    public int saveSharedFile(long userId, User newUser, FileNode fileNode, Long folderId) {
        if (!fileNode.isShared()) {
            return 460;
        }
        if (!fileNode.isFolder()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("nodeId").is(folderId));
            query.addCriteria(Criteria.where("userId").is(newUser.getUserId()));
            FileNode folder = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);

            fileNode.setFileName(myFileUtils.availableFileName(newUser.getUserId(), folderId, fileNode.getFileName()));
            fileNode.setId(null);
            fileNode.setUserId(newUser.getUserId());
            fileNode.setNodeId(seqService.getNextSeqId(newUser.getUserName()));
            fileNode.setParentId(folderId);
            fileNode.setShared(false);
            fileNode.setFavorites(false);
            fileNode.setFilePath(folder.getFilePath() + "/" + fileNode.getFileName());
            mongoTemplate.save(fileNode, FileServiceImpl.FILE_COLLECTION);
            md5Service.increaseIndex(fileNode.getMd5());
        } else {
            saveSharedFolder(userId, newUser, fileNode, folderId);
        }
        return 200;
    }

    @Override
    public int saveSharedFolder(long userId, User newUser, FileNode fileNode, Long folderId) {
        List<FileNode> fileNodeList = nodeRepository.getSubTree(userId, fileNode.getNodeId(), 0L).get(0).getDescendants();
        Query query = new Query();
        query.addCriteria(Criteria.where("nodeId").is(folderId));
        query.addCriteria(Criteria.where("userId").is(newUser.getUserId()));
        FileNode folder = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);

        fileNode.setFileName(myFileUtils.availableFolderName(newUser.getUserId(), folderId, fileNode.getFileName()));
        fileNode.setId(null);
        fileNode.setUserId(newUser.getUserId());
        fileNode.setNodeId(seqService.getNextSeqId(newUser.getUserName()));
        fileNode.setFilePath(folder.getFilePath() + "/" + fileNode.getFileName());
        fileNode.setParentId(folderId);
        fileNode.setShared(false);
        fileNode.setFavorites(false);
        mongoTemplate.save(fileNode, FileServiceImpl.FILE_COLLECTION);

        for (FileNode f : fileNodeList) {
            if (!f.isFolder()) {
                f.setId(null);
                f.setUserId(newUser.getUserId());
                f.setNodeId(seqService.getNextSeqId(newUser.getUserName()));
                f.setFilePath(fileNode.getFilePath() + "/" + fileNode.getFileName());
                f.setParentId(fileNode.getNodeId());
                f.setShared(false);
                fileNode.setFavorites(false);
                mongoTemplate.save(f);
                md5Service.increaseIndex(f.getMd5());
            } else {
                saveSharedFolder(userId, newUser, f, fileNode.getNodeId());
            }
        }
        return 200;
    }

    @Override
    public List querySharedFile(long userId, long nodeId, boolean isAll) {
        List<ShareFileDTO> folders = new ArrayList<>();
        List<ShareFileDTO> files = new ArrayList<>();

        List<Share> shares = nodeRepository.getShareSubTree(userId, nodeId, 0L).get(0).getDescendants();
        for (Share share : shares) {

            if (isAll) {
                String filePath = share.getSharePath();
                filePath = filePath.substring(1);
                filePath = filePath.substring(filePath.indexOf("/"));
                filePath = filePath.substring(1);
                String tmp = filePath.substring(0, filePath.indexOf("/"));
                filePath = filePath.substring(filePath.indexOf("/"));
                tmp = "/" + tmp + "_" + share.getUserId();
                filePath = "/board 全部文件" + tmp + filePath;
                share.setSharePath(filePath);
            }

            ShareFileDTO tmp = new ShareFileDTO(share, myFileUtils.getPrintSize(share.getFileSize()));
            if (share.isFolder()) {
                folders.add(tmp);
            } else {
                files.add(tmp);
            }
        }

        List<List> res = new ArrayList();
        res.add(folders);
        res.add(files);
        return res;
    }

    @Override
    public ParamDTO queryAll(long userId, long realUserId, String contentType) {
        Query query = new Query();
        List<ShareFileDTO> list = new ArrayList<>();
        Criteria criteria = Criteria.where("contentType").is(contentType);
        if (userId != 0) {
            query.addCriteria(Criteria.where("userId").is(userId));
        } else {
            criteria.norOperator(Criteria.where("userId").is(realUserId));
        }
        query.addCriteria(criteria);
        List<Share> shares = mongoTemplate.find(query, Share.class, SHARED_COLLECTION);
        for (Share share : shares) {
            ShareFileDTO tmp = new ShareFileDTO(share, myFileUtils.getPrintSize(share.getFileSize()));
            list.add(tmp);
        }
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(list);
        paramDTO.setContentSize(list.size());
        return paramDTO;
    }

    @Override
    public List queryPiazza(long realUserId) {
        List<ShareFileDTO> folders = new ArrayList<>();
        List<ShareFileDTO> files = new ArrayList<>();

        Query query = new Query();
        Criteria criteria = Criteria.where("parentId").is(1L);
        criteria.norOperator(Criteria.where("userId").is(realUserId));
        query.addCriteria(criteria);
        List<Share> shares = mongoTemplate.find(query, Share.class, SHARED_COLLECTION);

        for (Share share : shares) {
            String filePath = share.getSharePath();
            filePath = filePath.substring(1);
            filePath = filePath.substring(filePath.indexOf("/"));
            filePath = "/board 全部文件" + filePath;
            if (share.isFolder()) {
                filePath = filePath + "_" + share.getUserId();
            }
            share.setSharePath(filePath);

            ShareFileDTO tmp = new ShareFileDTO(share, myFileUtils.getPrintSize(share.getFileSize()));


            if (share.isFolder()) {
                folders.add(tmp);
            } else {
                files.add(tmp);
            }
        }
        List<List> res = new ArrayList();
        res.add(folders);
        res.add(files);
        return res;
    }

    @Override
    public List queryBrowsePath(List<String> content) {
        log.info("content的内容是：");
        log.info(content.toString());

        if (content.get(0).equals("board 全部文件")) {
            return queryBrowsePathAll(content);
        } else {
            return queryBrowsePathOne(content);
        }
    }

    @Override
    public List queryBrowsePathAll(List<String> content) {
        List<ParamDTO> res = new ArrayList<>();
        long parentId = 0;


        ParamDTO first = new ParamDTO();
        first.setFilename("board 全部文件");
        first.setNodeId(0L);
        first.setFilePath("/board 全部文件");
        res.add(first);

        if (content.size() == 1) {
            ParamDTO name = new ParamDTO();
            name.setUserId(0L);
            res.add(name);
            return res;
        }

        String tmp = content.get(1);
        long userId = Long.valueOf(tmp.substring(tmp.lastIndexOf("_") + 1));
        content.set(1, tmp.substring(0, tmp.lastIndexOf("_")));
        Query userQuey = new Query();
        userQuey.addCriteria(Criteria.where("userId").is(userId));
        User user = mongoTemplate.findOne(userQuey, User.class, UserServiceImpl.USER_COLLECTION);

        for (int i = 1; i < content.size(); i++) {
            ParamDTO paramDTO = new ParamDTO();
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId()));
            query.addCriteria(Criteria.where("fileName").is(content.get(i)));
            if (i == 1) {
                query.addCriteria(Criteria.where("parentId").is(1L));
                Share share = mongoTemplate.findOne(query, Share.class, SHARED_COLLECTION);

                paramDTO.setFilename(share.getFileName());
                paramDTO.setNodeId(share.getNodeId());
                paramDTO.setFilePath("/board 全部文件/" + share.getFileName() + "_" + user.getUserId());
                parentId = share.getNodeId();
            } else {
                query.addCriteria(Criteria.where("parentId").is(parentId));
                Share share = mongoTemplate.findOne(query, Share.class, SHARED_COLLECTION);

                String filePath = share.getSharePath();
                filePath = filePath.substring(1);
                filePath = filePath.substring(filePath.indexOf("/"));
                filePath = filePath.substring(1);
                tmp = filePath.substring(0, filePath.indexOf("/"));
                filePath = filePath.substring(filePath.indexOf("/"));
                tmp = "/" + tmp + "_" + share.getUserId();
                filePath = "/board 全部文件" + tmp + filePath;
                share.setSharePath(filePath);

                paramDTO.setFilename(share.getFileName());
                paramDTO.setNodeId(share.getNodeId());
                paramDTO.setFilePath(filePath);
                parentId = share.getNodeId();
            }
            res.add(paramDTO);
        }

        ParamDTO name = new ParamDTO();
        name.setUserId(user.getUserId());
        res.add(name);

        return res;
    }

    @Override
    public List queryBrowsePathOne(List<String> content) {
        List<ParamDTO> res = new ArrayList<>();
        long parentId = 0;

        Query userQuey = new Query();
        userQuey.addCriteria(Criteria.where("userName").is(content.get(0)));
        User user = mongoTemplate.findOne(userQuey, User.class, UserServiceImpl.USER_COLLECTION);

        for (int i = 0; i < content.size(); i++) {
            ParamDTO paramDTO = new ParamDTO();
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId()));
            query.addCriteria(Criteria.where("fileName").is(content.get(i)));
            query.addCriteria(Criteria.where("parentId").is(parentId));
            Share share = mongoTemplate.findOne(query, Share.class, SHARED_COLLECTION);

            paramDTO.setFilename(share.getFileName());
            paramDTO.setNodeId(share.getNodeId());
            paramDTO.setFilePath(share.getSharePath());
            parentId = share.getNodeId();
            res.add(paramDTO);
        }

        ParamDTO name = new ParamDTO();
        name.setUserId(user.getUserId());
        res.add(name);

        return res;
    }

    @Override
    public void updateMoveFile(FileNode newParent, FileNode fileNode) {
        long userId = fileNode.getUserId();
        long newParentId = newParent.getNodeId();
        if (!newParent.isShared()) {
            newParentId = 1L;
        }

        Query shareFolderQuery = new Query();
        shareFolderQuery.addCriteria(Criteria.where("userId").is(userId));
        shareFolderQuery.addCriteria(Criteria.where("nodeId").is(newParentId));
        Share shareFolder = mongoTemplate.findOne(shareFolderQuery, Share.class, SHARED_COLLECTION);

        Query q = new Query();
        q.addCriteria(Criteria.where("userId").is(userId));
        q.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
        Share share = mongoTemplate.findOne(q, Share.class, SHARED_COLLECTION);

        updateShareFolderSize(share.getUserId(), share.getParentId(), -share.getFileSize());

        share.setParentId(newParentId);
        share.setSharePath(shareFolder.getSharePath() + "/" + share.getFileName());
        mongoTemplate.save(share);
        updateShareFolderSize(share.getUserId(), share.getParentId(), share.getFileSize());


        if (share.isFolder()) {
            List<Share> list = nodeRepository.getShareSubTree(share.getUserId(), share.getNodeId(), 0L).get(0).getDescendants();
            updateSharePath(share, list);
        }
    }

    @Override
    public void updateRename(long userId, long nodeId, String newName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        Share share = mongoTemplate.findOne(query, Share.class, SHARED_COLLECTION);
        String sharePath = share.getSharePath();
        sharePath = sharePath.substring(0, sharePath.lastIndexOf("/")) + "/" + newName;
        share.setSharePath(sharePath);
        mongoTemplate.save(share);
        List<Share> children = nodeRepository.getShareSubTree(share.getUserId(), share.getNodeId(), 0L).get(0).getDescendants();
        if (children != null) {
            updateSharePath(share, children);
        }
    }

    @Override
    public String getUserName(Share share) {
        String[] list = share.getSharePath().split("/");
        return list[0];
    }

    @Override
    public String getFileName(Share share) {
        String[] list = share.getSharePath().split("/");
        return list[list.length - 1];
    }

    @Override
    public void updateSharePath(Share share, List<Share> list) {
        for (Share s : list) {
            String sName = s.getFileName();
            s.setSharePath(share.getSharePath() + "/" + sName);
            mongoTemplate.save(s, SHARED_COLLECTION);
            List<Share> children = nodeRepository.getShareSubTree(share.getUserId(), share.getNodeId(), 0L).get(0).getDescendants();
            if (children != null) {
                updateSharePath(s, children);
            }
        }
    }

    @Override
    public void updateShareFolderSize(long userId, long nodeId, long fileSize) {
        while (nodeId >= 1) {
            Query q = new Query();
            q.addCriteria(Criteria.where("userId").is(userId));
            q.addCriteria(Criteria.where("nodeId").is(nodeId));
            Share share = mongoTemplate.findOne(q, Share.class, SHARED_COLLECTION);
            Update update = new Update();
            update.set("fileSize", share.getFileSize() + fileSize);
            mongoTemplate.updateFirst(q, update, Share.class, SHARED_COLLECTION);
            nodeId = share.getParentId();
        }
    }


}
