package com.netdisk.service.impl;

import ch.qos.logback.core.util.FileUtil;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.ShareFileDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.module.User;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.FileService;
import com.netdisk.service.Md5Service;
import com.netdisk.service.SeqService;
import com.netdisk.service.SharedService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import com.netdisk.util.TypeComparator;
import com.zaxxer.hikari.util.FastList;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.CodecRegistryProvider;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    public static final String FILE_COLLECTION = "fileDocument";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SeqService seqService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    Md5Service md5Service;

    @Autowired
    MyFileUtils myFileUtils;

    @Autowired
    SharedService sharedService;

    @Autowired
    TypeComparator typeComparator;

    private final NodeRepository nodeRepository;

    public FileServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    // 返回当前用户所在的所有父目录
    @Override
    public List queryBrowsePath(User user, List content) {
        List<ParamDTO> res = new ArrayList<>();
        Long parentId = 1L;
        for (int i = 0; i < content.size(); i++) {
            ParamDTO paramDTO = new ParamDTO();
            if (i == 0) {
                // 返回用户的根目录
                paramDTO.setFilename(user.getUserName());
                paramDTO.setNodeId(1L);
                paramDTO.setFilePath("/" + user.getUserName());
                res.add(paramDTO);
            } else {
                FileNode fileNode = queryFolderByNameId(user.getUserId(), parentId, (String) content.get(i));
                if (fileNode == null) {
                    return null;
                }
                paramDTO.setFilename(fileNode.getFileName());
                paramDTO.setNodeId(fileNode.getNodeId());
                paramDTO.setFilePath(fileNode.getFilePath());
                parentId = fileNode.getNodeId();
                res.add(paramDTO);
            }
        }
        return res;
    }

    @Deprecated
    @Override
    public int uploadFile(User user, Long nodeId, MultipartFile[] list) {
        FileNode folder = queryFolderById(user.getUserId(), nodeId);
        for (MultipartFile file : list) {
            String fileName = file.getOriginalFilename();
//            fileName = availableFileName(user, nodeId, fileName);
            String md5 = null;
            try {
                md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String fileType = FilenameUtils.getExtension(file.getOriginalFilename());
            fileType = fileProperties.getIcon().containsKey(fileType) ? fileProperties.getIcon().get(fileType) : fileProperties.getOtherIcon();
            FileNode fileNode = new FileNode(null,
                    user.getUserId(),
                    seqService.getNextSeqId(user.getUserName()),
                    fileName,
                    folder.getFilePath() + "/" + fileName,
                    folder.getFilePath() + "/" + fileName,
                    false,
                    null,
                    folder.getNodeId(),
                    fileType,
                    false,
                    md5,
                    null,
                    null,
                    null,
                    false
            );
            FileNode origin = checkMd5(md5);
            if (origin != null) {
                fileNode.setStorePath(origin.getStorePath());
                mongoTemplate.save(fileNode, FILE_COLLECTION);
            } else {
                mongoTemplate.save(fileNode, FILE_COLLECTION);
                myFileUtils.saveFile(file, fileNode.getFilePath());
            }

        }
        return 200;
    }


    @Override
    public void insertFileNode(User user, Long nodeId, String fileName, String md5, long size) {
        FileNode folder = queryFolderById(user.getUserId(), nodeId);
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        fileType = fileType.toLowerCase(Locale.ROOT);
        fileType = fileProperties.getIcon().containsKey(fileType) ? fileProperties.getIcon().get(fileType) : fileProperties.getOtherIcon();
        String suf = fileName.substring(fileName.lastIndexOf(".") + 1);
        FileNode fileNode = new FileNode(null,
                user.getUserId(),
                seqService.getNextSeqId(user.getUserName()),
                fileName,
                folder.getFilePath() + "/" + fileName,
//                folder.getFilePath() + "/" + fileName,
                "/" + user.getUserName() + "/" + md5 + "." + suf,
                false,
                null,
                folder.getNodeId(),
                fileType,
                false,
                md5,
                null,
                size,
                myFileUtils.getTime(),
                false
        );
        if (isImage(fileNode.getContentType())) {
            String srcPath = fileProperties.getRootDir() + fileNode.getStorePath();
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String desPath = fileProperties.getTmpPath() + "/" + UUID.randomUUID() + suffix;
            fileNode.setBase64(myFileUtils.commpressPicForScale(srcPath, desPath, 50, 0.7));
        }
        mongoTemplate.save(fileNode, FILE_COLLECTION);
        updateFolderSize(user.getUserId(), fileNode.getParentId(), fileNode.getFileSize());
    }

    // 从nodeId开始，返回以maxDepth为层数的所有下层文件
    @Override
    public List<FileNode> sub(Long userId, Long nodeId, Long maxDepth) {
        return nodeRepository.getSubTree(userId, nodeId, maxDepth);
    }


    @Override
    public boolean createDir(User user, Long nodeId, String fileName) {
        if (queryFolderByNameId(user.getUserId(), nodeId, fileName) != null) {
            fileName = myFileUtils.availableFolderName(user.getUserId(), nodeId, fileName);
        }
        FileNode current = queryFolderById(user.getUserId(), nodeId);
        FileNode fileNode = new FileNode(null,
                user.getUserId(),
                seqService.getNextSeqId(user.getUserName()),
                fileName,
                current.getFilePath() + "/" + fileName,
                null,
                true,
                null,
                current.getNodeId(),
                fileProperties.getIcon().get("folder"),
                false,
                null,
                null,
                0L,
                myFileUtils.getTime(),
                false
        );
//        myFileUtils.createFolder(fileNode.getFilePath());
        mongoTemplate.save(fileNode, FILE_COLLECTION);
        return true;
    }


    @Override
    public void createUserFile(User user) {
        FileNode fileNode = new FileNode(null,
                user.getUserId(),
                seqService.getNextSeqId(user.getUserName()),
                user.getUserName(),
                "/" + user.getUserName(),
                "/" + user.getUserName(),
                true,
                null,
                0L,
                fileProperties.getIcon().get("folder"),
                false,
                null,
                null,
                0L,
                myFileUtils.getTime(),
                false
        );
        myFileUtils.createFolder(fileNode.getFilePath());
        mongoTemplate.save(fileNode, FILE_COLLECTION);
    }

    @Override
    public List<List> queryFolderContent(User user, Long nodeId) {
        List<FileNode> list = nodeRepository.getSubTree(user.getUserId(), nodeId, 0L).get(0).getDescendants();
        List<FileDTO> files = new ArrayList<>();
        List<FileDTO> folders = new ArrayList<>();
        for (FileNode f : list) {
            FileDTO fileDTO = new FileDTO(f);
            if (f.isFolder()) {
                fileDTO.setFileSizeInUnit(myFileUtils.getPrintSize(f.getFileSize()));
                fileDTO.setFileSize(f.getFileSize());
                folders.add(fileDTO);
            } else {
                if (isImage(f.getContentType())) {
                    if (f.getBase64() == null) {
//                    String srcPath = fileProperties.getRootDir() + f.getStorePath();
//                    String desPath = fileProperties.getTmpPath() + "/tmp.jpg";
//                    String base64 = myFileUtils.commpressPicForScale(srcPath,desPath,50,0.7);
//                        Query query = new Query();
//                        query.addCriteria(Criteria.where("userId").is(f.getUserId()));
//                        query.addCriteria(Criteria.where("nodeId").is(f.getNodeId()));
//                        Update update = new Update();
//                        update.set("base64", base64);
//                        mongoTemplate.findAndModify(query, update, FileNode.class);
                        fileDTO.setBase64(f.getBase64());
                    }
                    fileDTO.setBase64(f.getBase64());
                }
                fileDTO.setFileSizeInUnit(myFileUtils.getPrintSize(f.getFileSize()));
                fileDTO.setFileSize(f.getFileSize());
                files.add(fileDTO);
            }
        }
        Collections.sort(files, typeComparator);
        Collections.sort(folders, typeComparator);
        return Arrays.asList(folders, files);
    }

    @Override
    public Long checkFilePath(Long userId, String filePath) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("filePath").is(filePath));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
        if (fileNode != null) {
            return fileNode.getNodeId();
        } else {
            return null;
        }
    }

    @Override
    public ParamDTO queryFolderRootContent(User user) {
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setNodeId(1L);
        paramDTO.setUserName(user.getUserName());
        List content = queryFolderContent(user, 1L);
        ParamDTO res = new ParamDTO();
        res.setUserName(user.getUserName());
        res.setUserId(user.getUserId());
        res.setContent(content);
        return res;
    }

    @Override
    public FileNode queryFolderById(Long userId, Long nodeId) {
        Query query = new Query(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
    }

    @Override
    public FileNode queryFolderByNameId(Long userId, Long parentId, String fileName) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.addCriteria(Criteria.where("fileName").is(fileName));
        query.addCriteria(Criteria.where("isFolder").is(true));
        return mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
    }

    @Override
    public FileNode queryFileByNameId(Long userId, Long parentId, String fileName) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.addCriteria(Criteria.where("fileName").is(fileName));
        query.addCriteria(Criteria.where("isFolder").is(false));
        return mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
    }

    // 返回user下的所有目录和文件
    @Override
    public ParamDTO queryAll(Long userId, String contentType) {
        Query query = new Query(Criteria.where("userId").is(userId));
        if (!contentType.equals("")) {
            query.addCriteria(Criteria.where("contentType").is(contentType));
        }
        List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FILE_COLLECTION);
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (FileNode fileNode : fileNodeList) {
            FileDTO fileDTO = new FileDTO(fileNode);
            fileDTO.setFileSizeInUnit(myFileUtils.getPrintSize(fileNode.getFileSize()));
            fileDTOList.add(fileDTO);
        }
        Collections.sort(fileDTOList, typeComparator);
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(fileDTOList);
        paramDTO.setContentSize(fileDTOList.size());
        return paramDTO;
    }

    // 对文件星标
    @Override
    public int favoriteFile(Long userId, Long nodeId, Boolean isFavorites) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        Update update = new Update();
        update.set("isFavorites", isFavorites);
        mongoTemplate.updateFirst(query, update, FILE_COLLECTION);
        return 0;
    }

    // 返回所有星标文件
    @Override
    public ParamDTO queryFavorites(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("isFavorites").is(true));
        List<FileNode> fileNodeList = mongoTemplate.find(query, FileNode.class, FILE_COLLECTION);
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (FileNode fileNode : fileNodeList) {
            FileDTO fileDTO = new FileDTO(fileNode);
            fileDTO.setFileSizeInUnit(myFileUtils.getPrintSize(fileNode.getFileSize()));
            fileDTOList.add(fileDTO);
        }
        Collections.sort(fileDTOList, typeComparator);
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(fileDTOList);
        paramDTO.setContentSize(fileDTOList.size());
        return paramDTO;
    }

    // 返回所有分享文件
    @Override
    public ParamDTO queryShared(Long userId) {
        List<Share> shares = nodeRepository.getShareSubTree(userId, 1L, 0L).get(0).getDescendants();
        List<FileDTO> fileNodeList = new ArrayList<>();
        for (Share share : shares) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(share.getUserId()));
            query.addCriteria(Criteria.where("nodeId").is(share.getNodeId()));
            FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
            FileDTO fileDTO = new FileDTO(fileNode);
            fileDTO.setFileSizeInUnit(myFileUtils.getPrintSize(fileNode.getFileSize()));
            fileNodeList.add(fileDTO);
        }
        Collections.sort(fileNodeList, typeComparator);
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(fileNodeList);
        paramDTO.setContentSize(fileNodeList.size());
        return paramDTO;
    }


    // 返回用户所有的文件夹节点
    @Override
    public ParamDTO queryAllFolder(User user, Long nodeId) {
        ParamDTO paramDTO = new ParamDTO();
        List<FileNode> fileNodes = nodeRepository.getSubTree(user.getUserId(), nodeId, 0L, true).get(0).getDescendants();
        List<FileDTO> res = new ArrayList<>();
        for (FileNode fileNode : fileNodes) {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setNodeId(fileNode.getNodeId());
            fileDTO.setFileName(fileNode.getFileName());
            fileDTO.setFilePath(fileNode.getFilePath());
            if (queryFolderContent(user, fileNode.getNodeId()).get(0).size() == 0) {
                // 确定目录叶子节点
                fileDTO.setIsLeaf(true);
            } else {
                fileDTO.setIsLeaf(false);
            }
            res.add(fileDTO);
        }
        paramDTO.setContent(res);
        return paramDTO;
    }

    @Override
    public FileNode checkMd5(String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("md5").is(md5));
        query.addCriteria(Criteria.where("isFolder").is(false));
        return mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
    }

    @Override
    public int checkMoveFile(FileNode newParent, FileNode fileNode) {
        if (!newParent.isFolder()) {
            return 404;
        }
        if (Objects.equals(newParent.getNodeId(), fileNode.getParentId())) {
            return 459;
        }
        if (Objects.equals(newParent.getNodeId(), fileNode.getNodeId())) {
            return 459;
        }
        if (fileNode.isFolder()) {
            List<FileNode> childrenNode = nodeRepository.getSubTree(fileNode.getUserId(), fileNode.getNodeId(), null, true).get(0).getDescendants();
            for (FileNode node : childrenNode) {
                if (Objects.equals(node.getNodeId(), newParent.getNodeId())) {
                    return 459;
                }
            }
        }

        return 200;
    }

    @Override
    public int checkMoveFile(long newParentNodeId, long nodeId, long userId) {
        FileNode newParent = queryFolderById(userId, newParentNodeId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
        return checkMoveFile(newParent, fileNode);
    }

    @Override
    public int moveFile(Long userId, Long newParentNodeId, Long nodeId, boolean needCheck) {
        FileNode newParent = queryFolderById(userId, newParentNodeId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);

        if (needCheck) {
            int check = checkMoveFile(newParent, fileNode);
            if (check != 200) {
                return check;
            }
        }

        String fileName = null;
        if (fileNode.isFolder()) {
            fileName = myFileUtils.availableFolderName(userId, newParentNodeId, fileNode.getFileName());
        } else {
            fileName = myFileUtils.availableFileName(userId, newParentNodeId, fileNode.getFileName());
        }
        if (queryFolderByNameId(fileNode.getUserId(), newParentNodeId, fileNode.getFileName()) != null) {
            fileName = myFileUtils.availableFolderName(userId, newParentNodeId, fileName);
        }
        Update update = new Update();
        String newPath = newParent.getFilePath() + File.separator + fileName;
        update.set("parentId", newParent.getNodeId());
        update.set("filePath", newPath);
//        update.set("storePath", newPath);
        update.set("fileName", fileName);
        mongoTemplate.findAndModify(query, update, FileNode.class, FILE_COLLECTION);

        updateFolderSize(userId, newParent.getNodeId(), fileNode.getFileSize());
        updateFolderSize(userId, fileNode.getParentId(), -fileNode.getFileSize());

        if (fileNode.isShared()) {
            sharedService.updateMoveFile(newParent, fileNode);
        }

//        try {
//            Files.move(new File(fileProperties.getRootDir() + fileNode.getStorePath()).toPath(),
//                    new File(fileProperties.getRootDir() + newPath).toPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (fileNode.isFolder()) {
            fileNode.setFilePath(newPath);
            chChildPath(fileNode);
        }
        return 200;
    }

    @Override
    public void chChildPath(FileNode fileNode) {
        List<FileNode> children = nodeRepository.getSubTree(fileNode.getUserId(), fileNode.getNodeId(), 0L).get(0).getDescendants();
        for (FileNode child : children) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(child.getUserId()));
            query.addCriteria(Criteria.where("nodeId").is(child.getNodeId()));
            Update update = new Update();
            String newPath = fileNode.getFilePath() + File.separator + child.getFileName();
            update.set("filePath", newPath);
//            update.set("storePath", newPath);
            mongoTemplate.findAndModify(query, update, FileNode.class, FILE_COLLECTION);
            child.setFilePath(newPath);
            chChildPath(child);
        }
    }

    @Override
    public Long deleteFile(Long userId, Long nodeId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
        File file = new File(fileProperties.getRootDir() + fileNode.getStorePath());
        long filesize = 0L;
        if (fileNode.isFolder()) {
            List<FileNode> list = nodeRepository.getSubTree(userId, nodeId, null).get(0).getDescendants();
            log.info("list = " + list.toString());
            for (FileNode fn : list) {
                if (!fn.isFolder()) {
                    filesize += fn.getFileSize();
                    if (md5Service.decreaseIndex(fn.getMd5()) == 0) {
                        File f = new File(fileProperties.getRootDir() + fn.getStorePath());
                        f.delete();
                    }
                }
                Query q = new Query();
                q.addCriteria(Criteria.where("userId").is(fn.getUserId()));
                q.addCriteria(Criteria.where("nodeId").is(fn.getNodeId()));
                mongoTemplate.remove(q, FILE_COLLECTION);

                if (fn.isShared()) {
                    mongoTemplate.remove(q, Share.class, SharedServiceImpl.SHARED_COLLECTION);
                }

            }
            mongoTemplate.remove(query, FILE_COLLECTION);
//            FileUtils.deleteQuietly(file);
        } else {
            filesize += fileNode.getFileSize();
            mongoTemplate.remove(query, FILE_COLLECTION);
            if (md5Service.decreaseIndex(fileNode.getMd5()) == 0) {
                file.delete();
            }
        }

        if (fileNode.isShared()) {
            mongoTemplate.remove(query, Share.class, SharedServiceImpl.SHARED_COLLECTION);
        }

//        return filesize;
        updateFolderSize(userId, fileNode.getParentId(), -fileNode.getFileSize());
        return fileNode.getFileSize();
    }

    @Override
    public boolean isImage(String contentType) {
        return fileProperties.getIcon().get("picture").equals(contentType);
    }


    @Override
    public ParamDTO getDetail(Long userId, Long nodeId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
        ParamDTO paramDTO = new ParamDTO();
//        paramDTO.setUploadTime(fileNode.getUploadTime());
        paramDTO.setFilePath(fileNode.getFilePath());
        paramDTO.setFilename(fileNode.getFileName());
        paramDTO.setContentType(fileNode.getFileName().substring(fileNode.getFileName().lastIndexOf(".") + 1));
        paramDTO.setFileSize(fileNode.getFileSize());
        paramDTO.setFileSizeInUnit(myFileUtils.getPrintSize(fileNode.getFileSize()));

//        if (!fileNode.isFolder()) {
//            paramDTO.setSize(myFileUtils.getPrintSize(fileNode.getFileSize()));
//            paramDTO.setContentType(fileNode.getFileName().substring(fileNode.getFileName().lastIndexOf(".") + 1));
//        } else {
//            List<FileNode> list = nodeRepository.getSubTree(userId, nodeId, null).get(0).getDescendants();
//            long size = 0L;
//            for (FileNode f : list) {
//                if (!f.isFolder()) {
//                    size += f.getFileSize();
//                }
//            }
//            paramDTO.setSize(myFileUtils.getPrintSize(size));
//            paramDTO.setContentType("folder");
//        }

        return paramDTO;
    }

    @Override
    public List<FileNode> queryAllFiles(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, FileNode.class, FILE_COLLECTION);
    }

    // 修改名字
    @Override
    public boolean chName(FileNode fileNode, String newName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(fileNode.getUserId()));
        query.addCriteria(Criteria.where("nodeId").is(fileNode.getNodeId()));
        if (fileNode.isFolder()) {
            Update update = new Update();
            String filePath = fileNode.getFilePath();
            String newFilePath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + newName;
            update.set("fileName", newName);
            update.set("filePath", newFilePath);
//            update.set("storePath", newFilePath);
            mongoTemplate.findAndModify(query, update, FileNode.class, FILE_COLLECTION);
//            File src = new File(fileProperties.getRootDir() + fileNode.getFilePath());
//            File des = new File(fileProperties.getRootDir() + newFilePath);
//            src.renameTo(des);
            fileNode.setFilePath(newFilePath);
            chChildPath(fileNode);
        } else {
            String fileName = fileNode.getFileName();
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            newName = newName + "." + suffix;
            String filePath = fileNode.getFilePath();
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + newName;
            Update update = new Update();
            update.set("fileName", newName);
            update.set("filePath", filePath);
//            update.set("storePath", filePath);
            mongoTemplate.findAndModify(query, update, FileNode.class, FILE_COLLECTION);
//            File src = new File(fileProperties.getRootDir() + fileNode.getFilePath());
//            File des = new File(fileProperties.getRootDir() + filePath);
//            src.renameTo(des);
        }

        if (fileNode.isShared()) {
            sharedService.updateRename(fileNode.getUserId(), fileNode.getNodeId(), newName);
        }

        return true;

    }

    @Override
    public void updateFolderSize(long userId, long nodeId, long fileSize) {
        while (nodeId >= 1) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(userId));
            query.addCriteria(Criteria.where("nodeId").is(nodeId));
            FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FILE_COLLECTION);
            Update update = new Update();
            update.set("fileSize", fileNode.getFileSize() + fileSize);
            mongoTemplate.updateFirst(query, update, FileNode.class, FILE_COLLECTION);
            nodeId = fileNode.getParentId();
        }
    }

}
