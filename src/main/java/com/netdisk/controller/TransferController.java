package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.service.ChunkService;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;


@Api(value = "上传下载")
@Controller
@Slf4j
@CrossOrigin(origins = "http://192.168.1.169:9070", allowCredentials = "true")
public class TransferController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private SeqService seqService;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 创建上传任务
     * md5,uid, nodeId, userId, fileName
     */
    @GetMapping(value = "/file/createTask")
    @ResponseBody
    public Response createTask(
            @RequestParam(value = "hash") String md5,
            @RequestParam(value = "uid") String uuid,
            @RequestParam(value = "file_name") String fileName,
            @RequestParam(value = "user_id") Long userId,
            @RequestParam(value = "node_id") Long nodeId) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        ParamDTO res = new ParamDTO();
        if (nodeId == 0L) {
            return assembly.fail(453, null);
        }
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        if (fileNode == null || !fileNode.isFolder()) {
            return assembly.fail(453, null);
        }
        String contentType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(!fileProperties.getIcon().containsKey(contentType)){
            contentType = fileProperties.getOtherIcon();
        }
        Long serialNo = chunkService.createTask(md5, uuid, userId, nodeId, fileName);
        res.setSliceNo(serialNo);
        res.setContentType(contentType);
        return assembly.success(res);
    }

    /**
     * 上传分片
     * sliceNo, taskId, file
     */
    @PostMapping(value = "/file/uploadSlice")
    @ResponseBody
    public Response uploadSlice(@RequestParam(value = "name") String sliceName,
                                @RequestParam(value = "uid") String uuid,
                                @RequestParam(value = "chunk") MultipartFile file) {
        AssemblyResponse<Object> assembly = new AssemblyResponse<>();
        int res = chunkService.uploadSlice(file, uuid, sliceName);
        if (res == 200) {
            return assembly.success(null);
        } else {
            return assembly.fail(res, null);
        }
    }

    /**
     * merge所有分片
     * md5, uuid
     */
    @GetMapping(value = "/file/merge")
    @ResponseBody
    public Response merge(@RequestParam(value = "hash") String md5,
                          @RequestParam(value = "uid") String uid) {
        AssemblyResponse<Integer> assembly = new AssemblyResponse<>();
        int res = chunkService.merge(uid, md5);
        if (res != 200) {
            return assembly.fail(res, null);
        } else {
            return assembly.success(null);
        }
    }

    /**
     * 删除所有缓存文件
     */
    @GetMapping(value = "/file/abort")
    @ResponseBody
    public Response abort(@RequestParam(value = "hash") String md5,
                          @RequestParam(value = "uid") String uid) {
        AssemblyResponse<Integer> assembly = new AssemblyResponse<>();
        int res = chunkService.abort(uid, md5);
        if (res != 200) {
            return assembly.fail(res, null);
        } else {
            return assembly.success(null);
        }
    }


    /**
     * 文件分片下载
     * * @param range http请求头Range，用于表示请求指定部分的内容。
     * * 格式为：Range: bytes=start-end  [start,end]表示，即是包含请求头的start及end字节的内容
     */
    @RequestMapping(value = "/file/download", method = RequestMethod.GET)
    public void fileChunkDownload(@RequestHeader(value = "Range") String range,
                                  @RequestParam(value = "user_id") Long userId,
                                  @RequestParam(value = "node_id") Long nodeId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        chunkService.fileChunkDownload(range, fileProperties.getRootDir() + File.separator + fileNode.getFilePath(), request, response);
    }


    @GetMapping(value = "vdownload/**")
    public void vdownload(HttpServletRequest request, HttpServletResponse response) {
        chunkService.vDownload(request, response);
    }

    @GetMapping("/file/getMd5")
    @ResponseBody
    public Response getMd5(@RequestParam(value = "user_id") Long userId,
                           @RequestParam(value = "node_id") Long nodeId) {
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        return assembly.success(fileNode.getMd5());
    }

    @GetMapping(value = "vopen/**")
    public void vopen(
            HttpServletRequest request, HttpServletResponse response) {
        log.info("Enter vop controller ================");
        chunkService.vOpen(request, response);
    }


}
