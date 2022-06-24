package com.netdisk.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

@Api(value = "文件管理")
@Controller
@RequestMapping("/file")
@CrossOrigin(origins = {"http://172.17.0.1", "http://172.17.0.1:9070", "http://www.aijiangsb.com" ,"http://aijiangsb.com:9070",
        "https://172.17.0.1", "https://172.17.0.1:9070", "https://www.aijiangsb.com" ,"https://aijiangsb.com:9070",
        "https://www.aijiangsb.com:9070","http://www.aijiangsb.com:9070"}
        , allowCredentials = "true")
//@CrossOrigin(origins = {"http://192.168.1.169:9070"}, allowCredentials = "true")
public class FileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SeqService seqService;

    @Autowired
    private FileProperties fileProperties;

    /**
     * 新建目录并记录在数据库 userId, nodeId, FileName
     */
    @PostMapping("/newdir")
    @ResponseBody
    public Response createDir(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        boolean success = fileService.createDir(user, paramDTO.getNodeId(), paramDTO.getFilename());
        if (success) {
            return assembly.success("新目录创建成功");
        } else {
            return assembly.fail(451, "已存在同名文件夹");
        }
    }

    /**
     * 查询文件夹内容
     * userId, nodeId
     *
     * @return
     */
    @PostMapping("/query")
    @ResponseBody
    public Response queryFolder(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        return assembly.success(fileService.queryFolderContent(user, paramDTO.getNodeId()));
    }


    /**
     * 根据浏览目录，返回各级目录的id和路径
     * userId, content
     */
    @PostMapping(value = "/browsePath")
    @ResponseBody
    public Response browsePath(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        User user = userService.getUserById(paramDTO.getUserId());
        List res = fileService.queryBrowsePath(user, paramDTO.getContent());
        if (res == null) {
            return assembly.fail(453, null);
        } else {
            return assembly.success(res);
        }
    }

    /**
     * 返回用户的所有图片文件
     * userId
     */
    @PostMapping(value = "/queryImage")
    @ResponseBody
    public Response queryAllImage(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(), fileProperties.getIcon().get("picture")));
    }

    /**
     * 返回用户所有的视频文件
     * userId
     */
    @PostMapping(value = "/queryFilm")
    @ResponseBody
    public Response queryAllFilm(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(), fileProperties.getIcon().get("film")));
    }

    /**
     * 返回用户所有的种子文件
     * userId
     */
    @PostMapping(value = "/queryTorrent")
    @ResponseBody
    public Response queryAllTorrent(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(), fileProperties.getIcon().get("torrent")));
    }

    /**
     * 返回用户所有音乐文件
     * userId
     */
    @PostMapping(value = "/queryMusic")
    @ResponseBody
    public Response queryMusic(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAll(paramDTO.getUserId(), fileProperties.getIcon().get("music")));
    }

    /**
     * 返回用户所有的收藏文件及文件夹
     * userId
     */
    @PostMapping(value = "/queryFavorites")
    @ResponseBody
    public Response queryFavorites(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryFavorites(paramDTO.getUserId()));
    }

    /**
     * 返回用户所有的分享文件
     * userId
     */
    @PostMapping(value = "/queryShare")
    @ResponseBody
    public Response queryShare(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryShared(paramDTO.getUserId()));
    }

    /**
     * 收藏文件或文件夹
     * userId, nodeId, isFavorites
     */
    @PostMapping(value = "/favoriteFile")
    @ResponseBody
    public Response favoriteFile(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        fileService.favoriteFile(paramDTO.getUserId(), paramDTO.getNodeId(), paramDTO.getIsFavorites());
        return assembly.success("update success!");
    }

    /**
     * 查询该目录下的所有文件夹
     * userId, nodeId
     */
    @PostMapping(value = "/queryAllFolder")
    @ResponseBody
    public Response queryAllFolder(@RequestBody ParamDTO paramDTO) {
        User user = userService.getUserById(paramDTO.getUserId());
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryAllFolder(user, paramDTO.getNodeId()));
    }

    /**
     * 查看输入的路径是否存在
     * userId, filePath
     */
    @PostMapping(value = "checkPath")
    @ResponseBody
    public Response checkPath(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<Long> assembly = new AssemblyResponse<>();
        Long res = fileService.checkFilePath(paramDTO.getUserId(), paramDTO.getFilePath());
        if (res != null) {
            return assembly.success(res);
        } else {
            return assembly.fail(453, null);
        }
    }

    /**
     * 移动文件
     * userId, node_id, new_nodeId
     */
    @PostMapping(value = "moveFile")
    @ResponseBody
    public Response moveFile(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        int res = fileService.moveFile(paramDTO.getUserId(), paramDTO.getNewNodeId(), paramDTO.getNodeId(), true);
        if (res == 200) {
            return assembly.success("successfully");
        } else {
            return assembly.fail(res, "fail to move");
        }
    }

    /**
     * 批量移动文件
     * userId, file_list, new_nodeId
     */
    @PostMapping(value = "moveFiles")
    @ResponseBody
    public Response moveFiles(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        List<Long> fileList = paramDTO.getFileNodes();
        for (long nodeId : fileList) {
            int tmp = fileService.checkMoveFile(paramDTO.getNewNodeId(), nodeId, paramDTO.getUserId());
            if (tmp != 200) {
                return assembly.fail(tmp, "fail to move");
            }
        }
        for (long nodeId : fileList) {
            fileService.moveFile(paramDTO.getUserId(), paramDTO.getNewNodeId(), nodeId, false);
        }
        return assembly.success("successfully");
    }

    /**
     * 删除文件
     * userId, node_id
     */
    @PostMapping(value = "deleteFile")
    @ResponseBody
    public Response deleteFile(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        long deleteSize = fileService.deleteFile(paramDTO.getUserId(), paramDTO.getNodeId());
        userService.updateSize(paramDTO.getUserId(), -deleteSize);
        return assembly.success("successfully");
    }

    /**
     * 批量删除文件
     * userId, node_id
     */
    @PostMapping(value = "deleteFiles")
    @ResponseBody
    public Response deleteFiles(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        long deleteSize = 0L;
        List<Long> fileNodes = paramDTO.getFileNodes();
        for (long nodeId : fileNodes) {
            deleteSize += fileService.deleteFile(paramDTO.getUserId(), nodeId);
        }
        userService.updateSize(paramDTO.getUserId(), -deleteSize);
        return assembly.success("successfully");
    }

    /**
     * 查看文件详细信息
     */
    @GetMapping(value = "detail")
    @ResponseBody
    public Response detail(@RequestParam(value = "user_id") long userId,
                           @RequestParam(value = "node_id") long nodeId) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.getDetail(userId, nodeId));
    }

    /**
     * 修改文件、文件夹名称
     * userId, nodeId, fileName
     */
    @PostMapping(value = "/reName")
    @ResponseBody
    public Response reName(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        String newName = paramDTO.getFilename();
        FileNode fileNode = fileService.queryFolderById(paramDTO.getUserId(), paramDTO.getNodeId());
        String oldName = fileNode.getFileName();
        FileNode sameName = null;
        if (fileNode.isFolder()) {
            sameName = fileService.queryFolderByNameId(paramDTO.getUserId(), fileNode.getParentId(), newName);
            if (sameName != null) {
                return assembly.fail(451, null);
            }
            fileService.chName(fileNode, paramDTO.getFilename());
        } else {
            String suffix = oldName.substring(oldName.lastIndexOf(".") + 1);
            newName = newName + "." + suffix;
            sameName = fileService.queryFileByNameId(paramDTO.getUserId(), fileNode.getParentId(), newName);
            if (sameName != null) {
                return assembly.fail(452, null);
            }
            fileService.chName(fileNode, paramDTO.getFilename());
        }
        return assembly.success(null);

    }
}
