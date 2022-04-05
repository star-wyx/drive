package com.netdisk.controller;

import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(value = "文件管理")
@Controller
@RequestMapping("/file")
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private SeqService seqService;

    /**
     * 新建目录并记录在数据库 userName, nodeId, FileName
     */
    @PostMapping("/newdir")
    @ResponseBody
    public Response createDir(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if(fileService.createDir(paramDTO)){
            return assembly.success("新目录创建成功");
        }else{
            return assembly.fail(451,"已存在同名文件夹");
        }
    }

    /**
     *
     * 查询文件夹内容
     * userName, nodeId
     * @return
     */
    @PostMapping("/query")
    @ResponseBody
    public Response queryFolder(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryFolderContent(paramDTO));
    }

    /**
     * 上传文件，并存储在相应位置
     * @param files 文件
     * userName, nodeId
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public Response uploadFile(@RequestParam("files") MultipartFile[] files,
                               @RequestParam("user_name") String userName,
                               @RequestParam("node_id") Long nodeId
                               ){
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if (files[0].isEmpty()) {
            return assembly.fail(450, "empty file");
        }
        if(fileService.uploadFile(userName,nodeId,files) == 452){
            return assembly.fail(452,"已存在同名文件");
        }
        return assembly.success("upload successfully");
    }
}
