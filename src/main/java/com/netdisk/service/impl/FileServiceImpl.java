package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.CodecRegistryProvider;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import java.util.*;


@Service
public class FileServiceImpl implements FileService {

    public static final String FILE_COLLECTION = "fileDocument";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SeqService seqService;

    @Autowired
    FileProperties fileProperties;


    @Autowired
    MyFileUtils myFileUtils;

    private final NodeRepository nodeRepository;

    public FileServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public List queryBrowsePath(User user, List content) {
        List<ParamDTO> res = new ArrayList<>();
        Long parentId = 1L;
        for (int i = 0; i < content.size(); i++) {
            ParamDTO paramDTO = new ParamDTO();
            if (i == 0) {
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

    @Override
    public int uploadFile(User user, Long nodeId, MultipartFile[] list) {
        FileNode folder = queryFolderById(user.getUserId(), nodeId);
        for (MultipartFile file : list) {
            String originalFilenameName = file.getOriginalFilename();
            String suffix = originalFilenameName.substring(originalFilenameName.lastIndexOf("."));
            String reallyName = originalFilenameName.substring(0,originalFilenameName.lastIndexOf("."));
            StringBuilder sb = new StringBuilder();
            sb.append(reallyName);
            int i = 1;
            while (true) {
                FileNode fileNode = queryFileByNameId(user.getUserId(), nodeId, sb.toString());
                if(fileNode == null){
                    break;
                }else{
                    sb = new StringBuilder();
                    sb.append(reallyName).append("(").append(i).append(")").append(suffix);
                    i++;
                }
            }
            String fileName = sb.toString();
            String fileType = FilenameUtils.getExtension(file.getOriginalFilename());
            fileType = fileProperties.getIcon().containsKey(fileType) ? fileProperties.getIcon().get(fileType) : fileProperties.getOtherIcon();
            FileNode fileNode = new FileNode(null,
                    user.getUserId(),
                    seqService.getNextSeqId(user.getUserName()),
                    fileName,
                    folder.getFilePath() + "/" + fileName,
                    false,
                    null,
                    folder.getNodeId(),
                    fileType,
                    false
            );
            mongoTemplate.save(fileNode, FILE_COLLECTION);
            myFileUtils.saveFile(file, fileNode.getFilePath());
        }
        return 200;
    }

    @Override
    public List<FileNode> sub(Long userId, Long nodeId, Long maxDepth) {
        return nodeRepository.getSubTree(userId, nodeId, maxDepth);
    }


    @Override
    public boolean createDir(User user, Long nodeId, String fileName) {
        if (queryFolderByNameId(user.getUserId(), nodeId, fileName) != null) {
            return false;
        }
        FileNode current = queryFolderById(user.getUserId(), nodeId);
        FileNode fileNode = new FileNode(null,
                user.getUserId(),
                seqService.getNextSeqId(user.getUserName()),
                fileName,
                current.getFilePath() + "/" + fileName,
                true,
                null,
                current.getNodeId(),
                fileProperties.getIcon().get("folder"),
                false
        );
        myFileUtils.createFolder(fileNode.getFilePath());
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
                true,
                null,
                0L,
                fileProperties.getIcon().get("folder"),
                false
        );
        myFileUtils.createFolder(fileNode.getFilePath());
        mongoTemplate.save(fileNode, FILE_COLLECTION);
    }

    @Override
    public List queryFolderContent(User user, Long nodeId) {
        List<FileNode> list = nodeRepository.getSubTree(user.getUserId(), nodeId, 0L).get(0).getDescendants();
        List<FileDTO> files = new ArrayList<>();
        List<FileDTO> folders = new ArrayList<>();
        Collections.sort(list);
        for (FileNode f : list) {
            if (f.isFolder()) {
                folders.add(new FileDTO(f));
            } else {
                files.add(new FileDTO(f));
            }
        }
        return Arrays.asList(folders, files);
    }

    @Override
    public Response queryFolderRootContent(User user) {
        AssemblyResponse<ParamDTO> assemblyResponse = new AssemblyResponse();
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setNodeId(1L);
        paramDTO.setUserName(user.getUserName());
        List content = queryFolderContent(user, 1L);
        ParamDTO res = new ParamDTO();
        res.setUserName(user.getUserName());
        res.setUserId(user.getUserId());
        res.setContent(content);
        return assemblyResponse.success(res);
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

    @Override
    public ParamDTO queryAll(Long userId, String contentType) {
        Query query = new Query(Criteria.where("userId").is(userId));
        if (!contentType.equals("")) {
            query.addCriteria(Criteria.where("contentType").is(contentType));
        }
        List<FileDTO> fileDTOList = FileDTO.listConvert(mongoTemplate.find(query, FileNode.class, FILE_COLLECTION));
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(fileDTOList);
        paramDTO.setContentSize(fileDTOList.size());
        return paramDTO;
    }

    @Override
    public int favoriteFile(Long userId, Long nodeId, Boolean isFavorites) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        Update update = new Update();
        update.set("isFavorites", isFavorites);
        mongoTemplate.updateFirst(query, update, FILE_COLLECTION);
        return 0;
    }

    @Override
    public ParamDTO queryFavorites(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("isFavorites").is(true));
        List<FileDTO> fileDTOList = FileDTO.listConvert(mongoTemplate.find(query, FileNode.class, FILE_COLLECTION));
        ParamDTO paramDTO = new ParamDTO();
        paramDTO.setContent(fileDTOList);
        paramDTO.setContentSize(fileDTOList.size());
        return paramDTO;
    }

    @Override
    public ParamDTO queryAllFolder(Long userId, Long nodeId) {
        ParamDTO paramDTO = new ParamDTO();
        List<FileNode> fileNodes = nodeRepository.getSubTree(userId, nodeId, 0L, true).get(0).getDescendants();
        List<FileDTO> res = new ArrayList<>();
        for (FileNode fileNode : fileNodes) {
            FileDTO fileDTO = new FileDTO(fileNode);
            res.add(fileDTO);
        }
        paramDTO.setContent(res);
        return paramDTO;
    }
}
