package com.netdisk.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
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

import java.util.List;

@Api(value = "文件管理")
@Controller
@RequestMapping("/file")
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
public class FileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SeqService seqService;

    /**
     * 新建目录并记录在数据库 userId, nodeId, FileName
     */
    @PostMapping("/newdir")
    @ResponseBody
    public Response createDir(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        boolean success = fileService.createDir(user, paramDTO.getNodeId(), paramDTO.getFilename());
        if(success){
            return assembly.success("新目录创建成功");
        }else{
            return assembly.fail(451,"已存在同名文件夹");
        }
    }

    /**
     *
     * 查询文件夹内容
     * userId, nodeId
     * @return
     */
    @PostMapping("/query")
    @ResponseBody
    public Response queryFolder(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        return assembly.success(fileService.queryFolderContent(user,paramDTO.getNodeId()));
    }

    /**
     * 上传文件，并存储在相应位置
     * @param files 文件
     * userId, nodeId
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public Response uploadFile(@RequestParam("files") MultipartFile[] files,
                               @RequestParam("user_id") Long userId,
                               @RequestParam("node_id") Long nodeId
                               ){
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(userId);
        if (files[0].isEmpty()) {
            return assembly.fail(450, "empty file");
        }
        fileService.uploadFile(user,nodeId,files);
        return assembly.success("upload successfully");
    }


    /**
     * 根据浏览目录，返回各级目录的id和路径
     * userId, content
     */
    @PostMapping(value = "/browsePath")
    @ResponseBody
    public Response browsePath(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        List res = fileService.queryBrowsePath(user,paramDTO.getContent());
        if(res == null){
            return assembly.fail(453,null);
        }else{
            return assembly.success(res);
        }
    }

    /**
     * 返回用户的所有图片文件
     * userId
     */
    @PostMapping(value = "/queryImage")
    @ResponseBody
    public Response queryAllImage(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(),"image"));
    }

    /**
     * 返回用户所有的视频文件
     * userId
     */
    @PostMapping(value = "/queryFilm")
    @ResponseBody
    public Response queryAllFilm(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(),"film"));
    }

    /**
     * 返回用户所有的种子文件
     * userId
     */
    @PostMapping(value = "/queryTorrent")
    @ResponseBody
    public Response queryAllTorrent(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(),"file-earmark-arrow-down"));
    }

    /**
     * 返回用户所有的收藏文件及文件夹
     * userId
     */
    @PostMapping(value = "/queryFavorites")
    @ResponseBody
    public Response queryFavorites(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryFavorites(paramDTO.getUserId()));
    }

    /**
     * 收藏文件或文件夹
     * userId, nodeId, isFavorites
     */
    @PostMapping(value = "/favoriteFile")
    @ResponseBody
    public Response favoriteFile(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        fileService.favoriteFile(paramDTO.getUserId(),paramDTO.getNodeId(),paramDTO.getIsFavorites());
        return assembly.success("update success!");
    }

    /**
     * 查询该目录下的所有文件夹
     */
    @PostMapping(value = "/queryAllFolder")
    @ResponseBody
    public Response queryAllFolder(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAllFolder(paramDTO.getUserId(), paramDTO.getNodeId()));
    }
}
