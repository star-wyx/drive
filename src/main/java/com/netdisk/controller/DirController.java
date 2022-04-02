package com.netdisk.controller;

import com.netdisk.pojo.DTO.DirDTO;
import com.netdisk.pojo.DTO.FileDTO;
import com.netdisk.pojo.Dir;
import com.netdisk.pojo.File;
import com.netdisk.service.DirService;
import com.netdisk.service.FileService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dir")
@CrossOrigin(origins="http://192.168.1.169:9070", allowCredentials = "true")
public class DirController {

    @Autowired
    private DirService dirService;

    @Autowired
    private FileService fileService;

    /**
     *
     * @param map:user_id,parent_id,dir_name
     * 在目标文件夹下新建文件夹
     */

    @PostMapping("/newdir")
    @ResponseBody
    public Response NewDir(@RequestBody Map<String,Object> map) throws IOException {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        List<Dir> dirList = dirService.querySubDir(map);
        for(Dir dir:dirList){
            if(dir.getDirName().equals(map.get("dir_name"))){
                return assembly.fail(500,"已存在同名文件夹！");
            }
        }
        int res = dirService.NewFolder(map);
        if(res!=0){
            return assembly.success(null);
        }else{
            return assembly.fail(500,null);
        }
    }

    /**
     *
     * @param map:parent_id, user_id
     * 返回该dir下的所有文件夹和文件。
     */

    @PostMapping("/query")
    @ResponseBody
    public Response querySub(@RequestBody Map<String, Object> map){
        AssemblyResponse<List> assembly = new AssemblyResponse<>();
        List<Dir> dirList = dirService.querySubDir(map);
        List<File> fileList = fileService.queryByDir(map);
        List<DirDTO> dirDTOS = new ArrayList<>();
        List<FileDTO> fileDTOS = new ArrayList<>();
        for(Dir dir:dirList){
            dirDTOS.add(new DirDTO(dir));
        }
        for(File file:fileList){
            fileDTOS.add(new FileDTO(file));
        }
        List<Object> res = new LinkedList<>();
        res.add(dirDTOS);
        res.add(fileDTOS);
        return assembly.success(res);
    }
}
