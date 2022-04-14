package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.service.ChunkService;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;


@Api(value = "上传下载")
@Controller
@RequestMapping("/file")
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

    /**
     * 创建上传任务
     * md5,uid, nodeId, userId, fileName
     */
    @GetMapping(value = "/createTask")
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
        Long serialNo = chunkService.createTask(md5, uuid, userId, nodeId, fileName);
        res.setSliceNo(serialNo);
        res.setContentType(fileProperties.getIcon().get(contentType));
        return assembly.success(res);
    }

    /**
     * 上传分片
     * sliceNo, taskId, file
     */
    @PostMapping(value = "uploadSlice")
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
    @GetMapping(value = "merge")
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
    @GetMapping(value = "abort")
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
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void fileChunkDownload(@RequestHeader(value = "Range") String range,
                                  @RequestParam(value = "user_id") Long userId,
                                  @RequestParam(value = "node_id") Long nodeId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        chunkService.fileChunkDownload(range, fileProperties.getRootDir() + File.separator + fileNode.getFilePath(), request, response);
    }

}
