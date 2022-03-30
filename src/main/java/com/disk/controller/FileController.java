package com.disk.controller;

import com.disk.entity.Dir;
import com.disk.service.DirService;
import com.disk.service.FileService;
import com.disk.util.AssemblyResponse;
import com.disk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/file")
@CrossOrigin(origins = "http://192.168.1.169:9070", allowCredentials = "true")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private DirService dirService;

//    @PostMapping("/upload")
//    @ResponseBody
//    public Response uploadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        CommonsMultipartResolver cmr = new CommonsMultipartResolver(request.getServletContext());
//        AssemblyResponse<Integer> assembly = new AssemblyResponse<>();
//        if(cmr.isMultipart(request)){
//            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
//            Iterator<String> files = mRequest.getFileNames();
//            if(!files.hasNext()){
//                return assembly.fail(1,null);
//            }
//            while(files.hasNext()){
//                MultipartFile mFile = mRequest.getFile(files.next());
//                if(mFile!=null){
//                    String fileName = UUID.randomUUID()+mFile.getOriginalFilename();
//                    String path = "/Users/star_wyx/Desktop/File/"+fileName; //命名规则，后期如何查询。
//                    File localFile = new File(path);
//                    mFile.transferTo(localFile);
//                    request.setAttribute("fileUrl",path);
//
//                    Map<String,Object> map = new HashMap<>();
//                    String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//                    map.put("file_name",fileName);
//                    map.put("file_path",path);
//                    map.put("upload_time", Timestamp.valueOf(nowTime));
//                    map.put("uid",request.getParameter("user_id"));
//                    fileService.uploadFile(map);
//                }
//            }
//            return assembly.success(null);
//        }
//        return assembly.fail(1,null);
//    }

    /**
     * @param
     * @throws IOException
     */
    @PostMapping("/upload")
    @ResponseBody
    public Response uploadFile(@RequestParam(value = "files") List<CommonsMultipartFile> files, @RequestParam("user_id") int user_id, @RequestParam("dir_id") int dir_id) throws IOException {
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if (files.get(0).isEmpty()) {
            return assembly.fail(450, "empty file");
        }
        for (MultipartFile f : files) {
            if (!f.isEmpty()) {
//                String fileName = UUID.randomUUID() + f.getOriginalFilename();
                String fileName = f.getOriginalFilename();
                if (fileService.queryFileByDirAndName(dir_id, fileName) != null) {
                    return assembly.fail(500, "存在同名文件");
                }
                Dir dir = dirService.queryDir(dir_id);
                String store_path = dir.getStorePath() + "/" + fileName; //todo 命名规则，后期如何查询。
                File localFile = new File(store_path);
                f.transferTo(localFile);

                Map<String, Object> map = new HashMap<>();
                String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                map.put("file_name", fileName);
                map.put("file_path", dir.getDirPath()+"/"+fileName);
                map.put("store_path", store_path);
                map.put("upload_time", Timestamp.valueOf(nowTime));
                map.put("uid", user_id);
                map.put("dir_id",dir_id);
                fileService.uploadFile(map);
            }
        }
        return assembly.success("upload successfully");
    }

}
