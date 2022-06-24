package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.ShareFileDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Share;
import com.netdisk.module.User;
import com.netdisk.repository.NodeRepository;
import com.netdisk.service.FileService;
import com.netdisk.service.SharedService;
import com.netdisk.service.UserService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.SharedServiceImpl;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import io.swagger.annotations.Api;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Api(value = "分享")
@Controller
@RequestMapping("/share")
@CrossOrigin(origins = {"http://172.17.0.1", "http://172.17.0.1:9070", "http://www.aijiangsb.com" ,"http://aijiangsb.com:9070",
        "https://172.17.0.1", "https://172.17.0.1:9070", "https://www.aijiangsb.com" ,"https://aijiangsb.com:9070",
        "https://www.aijiangsb.com:9070","http://www.aijiangsb.com:9070"}
        , allowCredentials = "true")
//@CrossOrigin(origins = {"http://192.168.1.169:9070"}, allowCredentials = "true")
public class ShareController {

    @Autowired
    UserService userService;

    @Autowired
    FileService fileService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    NodeRepository nodeRepository;

    @Autowired
    SharedService sharedService;

    @Autowired
    FileProperties fileProperties;

    /**
     * 更改文件可分享状态
     * nodeId, userId, isShared
     */
    @PostMapping("/shareFile")
    @ResponseBody
    public Response shareFile(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        if (paramDTO.getIsShared()) {
            sharedService.shareFile(paramDTO.getUserId(), paramDTO.getNodeId());
            userService.setHaveShared(paramDTO.getUserId(), true);
        } else {
            sharedService.cancelShare(paramDTO.getUserId(), paramDTO.getNodeId());
            List<Share> children = nodeRepository.getShareSubTree(paramDTO.getUserId(), 1L, 0L).get(0).getDescendants();
            if (children == null) {
                userService.setHaveShared(paramDTO.getUserId(), false);
            }
        }
        return assembly.success(null);
    }

    /**
     * 批量更改文件可分享状态
     * file_list, userId, isShared
     */
    @PostMapping("/shareFiles")
    @ResponseBody
    public Response shareFiles(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        List<Long> list = paramDTO.getFileNodes();
        if (paramDTO.getIsShared()) {
            for (Long nodeId : list) {
                Query query = new Query();
                query.addCriteria(Criteria.where("userId").is(paramDTO.getUserId()));
                query.addCriteria(Criteria.where("nodeId").is(nodeId));
                FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
                if (fileNode.isShared()) {
                    sharedService.cancelShare(paramDTO.getUserId(), nodeId);
                }
                sharedService.shareFile(paramDTO.getUserId(), nodeId);
            }
            userService.setHaveShared(paramDTO.getUserId(), true);
        } else {
            for (Long nodeId : list) {
                sharedService.cancelShare(paramDTO.getUserId(), nodeId);
            }
            List<Share> children = nodeRepository.getShareSubTree(paramDTO.getUserId(), 1L, 0L).get(0).getDescendants();
            if (children == null) {
                userService.setHaveShared(paramDTO.getUserId(), false);
            }
        }
        return assembly.success(null);
    }

    /**
     * 查看分享文件
     * userId, nodeId, isAll, realUserId
     */
    @PostMapping("/query")
    @ResponseBody
    public Response query(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        if (paramDTO.getUserId() == 0L && paramDTO.getNodeId() == 0L) {
            return assembly.success(sharedService.queryPiazza(paramDTO.getRealUserId()));
        } else {
            return assembly.success(sharedService.querySharedFile(paramDTO.getUserId(), paramDTO.getNodeId(), paramDTO.getIsAll()));
        }
    }

    /**
     * 查看所有图片
     * userId
     */
    @PostMapping("/queryImage")
    @ResponseBody
    public Response queryImage(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        return assembly.success(sharedService.queryAll(paramDTO.getUserId(), paramDTO.getRealUserId(), fileProperties.getIcon().get("picture")));
    }

    /**
     * 查看所有视频
     * userId
     */
    @PostMapping("/queryVideo")
    @ResponseBody
    public Response queryVideo(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        return assembly.success(sharedService.queryAll(paramDTO.getUserId(), paramDTO.getRealUserId(), fileProperties.getIcon().get("film")));
    }

    /**
     * 查看所有音乐
     * userId
     */
    @PostMapping("/queryMusic")
    @ResponseBody
    public Response queryMusic(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        return assembly.success(sharedService.queryAll(paramDTO.getUserId(), paramDTO.getRealUserId(), fileProperties.getIcon().get("music")));
    }

    /**
     * 转存文件
     * userId, nodeId, newUserId, newNodeId
     */
    @PostMapping("/saveSharedFile")
    @ResponseBody
    public Response saveSharedFile(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(paramDTO.getUserId()));
        query.addCriteria(Criteria.where("nodeId").is(paramDTO.getNodeId()));
        FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
        FileNode des = fileService.queryFolderById(paramDTO.getNewUserId(), paramDTO.getNewNodeId());
        if (!des.isFolder()) {
            return assembly.fail(461, "destination is not a folder");
        }
        User newUser = userService.getUserById(paramDTO.getNewUserId());
        if (userService.availableSpace(paramDTO.getNewUserId()) < fileNode.getFileSize()) {
            return assembly.fail(455, "not enough space");
        }else{
            userService.updateSize(newUser.getUserId(), fileNode.getFileSize());
        }

        sharedService.saveSharedFile(paramDTO.getUserId(), newUser, fileNode, paramDTO.getNewNodeId());

        return assembly.success(null);
    }

    /**
     * 批量转存文件
     * , newUserId, newNodeId
     */
    @PostMapping("/saveSharedFiles")
    @ResponseBody
    public Response saveSharedFiles(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        FileNode des = fileService.queryFolderById(paramDTO.getNewUserId(), paramDTO.getNewNodeId());
        if (!des.isFolder()) {
            return assembly.fail(461, "destination is not a folder");
        }

        long fileSize = 0;
        List<FileNode> list = new ArrayList<>();
        for (int i = 0; i < paramDTO.getShareFileDTOS().size(); i++) {
            LinkedHashMap map = (LinkedHashMap) paramDTO.getShareFileDTOS().get(i);
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(map.get("user_id")));
            query.addCriteria(Criteria.where("nodeId").is(map.get("node_id")));
            FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            list.add(fileNode);
            fileSize += fileNode.getFileSize();
        }
        User newUser = userService.getUserById(paramDTO.getNewUserId());
        if (userService.availableSpace(paramDTO.getNewUserId()) < fileSize) {
            return assembly.fail(455, "not enough space");
        }else{
            userService.updateSize(newUser.getUserId(), fileSize);
        }

        for (FileNode fileNode : list) {
            sharedService.saveSharedFile(fileNode.getUserId(), newUser, fileNode, paramDTO.getNewNodeId());
        }

        return assembly.success(null);
    }

    /**
     * 查询所有分享文件的用户
     */
    @GetMapping("/queryUser")
    @ResponseBody
    public Response queryUser(@RequestParam(value = "user_id") Long userId) {
        AssemblyResponse assembly = new AssemblyResponse();
        return assembly.success(userService.querySharedUser(userId));
    }

    /**
     * 根据浏览目录，返回各级目录的id和路径
     * content
     */
    @PostMapping(value = "/browsePath")
    @ResponseBody
    public Response browsePath(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        List res = sharedService.queryBrowsePath(paramDTO.getContent());
        if (res == null) {
            return assembly.fail(453, null);
        } else {
            return assembly.success(res);
        }
    }

}
