package com.disk.controller;

import com.disk.service.FileService;
import com.disk.util.AssemblyResponse;
import com.disk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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
@CrossOrigin("*")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ResponseBody
    public Response uploadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommonsMultipartResolver cmr = new CommonsMultipartResolver(request.getServletContext());
        AssemblyResponse<Integer> assembly = new AssemblyResponse<>();
        if(cmr.isMultipart(request)){
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            Iterator<String> files = mRequest.getFileNames();
            if(!files.hasNext()){
                return assembly.fail(1,null);
            }
            while(files.hasNext()){
                MultipartFile mFile = mRequest.getFile(files.next());
                if(mFile!=null){
                    String fileName = UUID.randomUUID()+mFile.getOriginalFilename();
                    String path = "/Users/star_wyx/Desktop/File/"+fileName;
                    File localFile = new File(path);
                    mFile.transferTo(localFile);
                    request.setAttribute("fileUrl",path);

                    Map<String,Object> map = new HashMap<>();
                    String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    map.put("file_name",fileName);
                    map.put("file_path",path);
                    map.put("upload_time", Timestamp.valueOf(nowTime));
                    map.put("uid",request.getParameter("user_id"));
                    fileService.uploadFile(map);
                }
            }
            return assembly.success(null);
        }
        return assembly.fail(1,null);
    }


}