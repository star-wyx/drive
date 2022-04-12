package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
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
        int res = chunkService.merge(uid,md5);
        if(res != 200){
            return assembly.fail(res,null);
        }else{
            return assembly.success(null);
        }
    }
}
