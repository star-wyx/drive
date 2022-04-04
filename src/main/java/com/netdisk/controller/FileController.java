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
        AssemblyResponse<List<FileNode>> assembly = new AssemblyResponse<>();
        return assembly.success(fileService.queryFolderContent(paramDTO));
    }

//    @Autowired
//    private DirService dirService;

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
