package com.netdisk.controller;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Mp4;
import com.netdisk.module.UploadRecord;
import com.netdisk.service.*;
import com.netdisk.service.impl.ChunkServiceImpl;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.FfmpegUtil;
import com.netdisk.util.MyFileUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private Mp4Service mp4Service;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private MyFileUtils myFileUtils;

    @Autowired
    private FfmpegUtil ffmpegUtil;

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
            @RequestParam(value = "node_id") Long nodeId,
            @RequestParam(value = "file_size") Long fileSize) {
        AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
        ParamDTO res = new ParamDTO();
        if (nodeId == 0L) {
            return assembly.fail(453, null);
        }
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        if (fileNode == null || !fileNode.isFolder()) {
            return assembly.fail(453, null);
        }
        if (userService.availableSpace(userId) < fileSize) {
            return assembly.fail(455, null);
        }
        String contentType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!fileProperties.getIcon().containsKey(contentType)) {
            contentType = fileProperties.getOtherIcon();
        }
        Long serialNo = chunkService.createTask(md5, uuid, userId, nodeId, fileName);
        res.setSliceNo(serialNo);
        res.setContentType(fileProperties.getIcon().get(contentType));
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

    @GetMapping("/file/getOpenMd5")
    @ResponseBody
    public Response getOpenMd5(@RequestParam(value = "user_id") Long userId,
                               @RequestParam(value = "node_id") Long nodeId) {
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        String fileName = fileNode.getFileName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
//        if (fileNode.getContentType().equals(fileProperties.getIcon().get("film"))
//                && !suffix.equalsIgnoreCase("mp4")) {
//            Mp4 mp4 = mp4Service.queryByOtherMd5(fileNode.getMd5());
//            if (mp4 != null && mp4.getStatus().equals("DONE")) {
//                return assembly.success(mp4.getMd5());
//            }
////            else if (mp4 != null && mp4.getStatus().equals("ING")) {
////                return assembly.set(301, "transcoding ing");
////            }
//            else {
//                Map<String, String> map = myFileUtils.getEncodingFormat(fileProperties.getRootDir() + fileNode.getStorePath());
//                return assembly.fail(300, map.get("Video"));
//            }
//        }
        if (fileNode.getContentType().equals(fileProperties.getIcon().get("film"))) {
            Map<String, String> map = ffmpegUtil.getEncodingFormat(fileProperties.getRootDir() + fileNode.getStorePath());
            AssemblyResponse<ParamDTO> res = new AssemblyResponse<>();
            ParamDTO paramDTO = new ParamDTO();
            paramDTO.setCodec(map.get("Video"));
            paramDTO.setMd5(fileNode.getMd5());
            return res.fail(300,paramDTO);
        }else{
            return assembly.success(fileNode.getMd5());
        }
//        return assembly.success(fileNode.getMd5());
    }

    @GetMapping("/file/transcode")
    @ResponseBody
    public Response transcode(@RequestParam(value = "user_id") Long userId,
                              @RequestParam(value = "node_id") Long nodeId) {
        FileNode fileNode = fileService.queryFolderById(userId, nodeId);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        String fileName = fileNode.getFileName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!suffix.equalsIgnoreCase("mp4")) {
            String storePath = fileProperties.getRootDir() + fileNode.getStorePath();
            String filePath = fileNode.getFilePath();
            File file = new File(storePath);
            File folder = new File(fileProperties.getMp4Dir());
//            File folder = new File(fileProperties.getMp4Dir() + File.separator + filePath.substring(0, filePath.lastIndexOf("/")));
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String mp4Path = fileProperties.getMp4Dir() + File.separator + fileNode.getMd5() + ".mp4";
            String md5 = null;
//            String mp4Path = fileProperties.getMp4Dir() + File.separator + filePath.substring(0, filePath.lastIndexOf("/")) + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + ".mp4";
            File mp4 = new File(mp4Path);
            if (!mp4Service.checkUserTask(userId, fileNode.getMd5())) {
                return assembly.fail(457, "you have another trasncode task");
            }
            Mp4 mp4Record = mp4Service.queryByOtherMd5(fileNode.getMd5());
            if (mp4Record == null) {
                mp4Service.add(mp4.getName(), " ", mp4.getAbsolutePath(), fileNode.getMd5(), "ING", userId);
                try {
                    ffmpegUtil.convertToMp4(file, mp4);

//                    ffmpegUtil.convert(storePath, mp4Path, ffmpegUtil.getVideoFormat(storePath));

                    md5 = MyFileUtils.getMD5(mp4);
                    mp4Service.changeStatus(fileNode.getMd5(), "DONE");
                    mp4Service.setMd5(fileNode.getMd5(), md5);
                    return assembly.success(md5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mp4Record.getStatus().equals("ING")) {
                while (!mp4Service.queryByOtherMd5(fileNode.getMd5()).getStatus().equals("DONE")) {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return assembly.success(mp4Service.queryByOtherMd5(fileNode.getMd5()).getMd5());
            } else if (mp4Record.getStatus().equals("DONE")) {
                return assembly.success(mp4Record.getMd5());
            }
//            mp4Service.add(mp4.getName(), md5, mp4.getAbsolutePath(), fileNode.getMd5());

        }
        return assembly.fail(301, "no need to transcode");
    }

    @GetMapping(value = "vopen/**")
    public void vopen(
            HttpServletRequest request, HttpServletResponse response) {
        log.info("Enter vop controller ================");
        chunkService.vOpen(request, response);
    }

    @PostMapping("/setHistory")
    @ResponseBody
    public Response setHistory(@RequestBody UploadRecord uploadRecord) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(uploadRecord.getUserId()));
        query.addCriteria(Criteria.where("id").is(uploadRecord.getId()));
        UploadRecord record = mongoTemplate.findOne(query, UploadRecord.class, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
        if (record != null) {
            mongoTemplate.remove(record, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
        }
        uploadRecord.setRecordDate(new Date());
        mongoTemplate.save(uploadRecord);
        AssemblyResponse assembly = new AssemblyResponse();
        return assembly.success(null);
    }

    @PostMapping("/getHistory")
    @ResponseBody
    public Response getHistory(@RequestBody ParamDTO paramDTO) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(paramDTO.getUserId()));
        List<UploadRecord> list = mongoTemplate.find(query, UploadRecord.class, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
        for (UploadRecord uploadRecord : list) {
            uploadRecord.setUserId(null);
            uploadRecord.setRecordDate(null);
        }
        Collections.reverse(list);
        AssemblyResponse<List> assembly = new AssemblyResponse();
        return assembly.success(list);
    }

    @PostMapping("/deleteHistory")
    @ResponseBody
    public Response deleteHistory(@RequestBody UploadRecord uploadRecord) {
        Query query = new Query();
        AssemblyResponse assembly = new AssemblyResponse();
        query.addCriteria(Criteria.where("id").is(uploadRecord.getId()));
        mongoTemplate.remove(query, UploadRecord.class, ChunkServiceImpl.UPLOADRECORD_COLLECTION);
        return assembly.success(null);
    }


    @GetMapping(value = "vavatar/**")
    public void avatar(HttpServletRequest request, HttpServletResponse response) {
        String url = null;
        try {
            url = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("avatar url is: " + url);
        Pattern pattern = Pattern.compile("(.*/vavatar/)(\\d*)(.*?)");
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        String userId = matcher.group(2);
        log.info("url is: " + url + " userId is: " + userId);

//        String usrId = url.substring(url.lastIndexOf("/") + 1);
        File avatar = new File(fileProperties.getProfileDir() + File.separator + userId + ".png");
        response.setStatus(response.SC_OK);
        response.setHeader("Content-Length", String.valueOf(avatar.length()));
        response.setHeader("Content-Type", request.getServletContext().getMimeType(avatar.getName()));

        OutputStream os = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(avatar));
            byte[] buff = new byte[1024];
            os = response.getOutputStream();
            int i = 0;
            while (i != -1) {
                i = bis.read(buff);
                os.write(buff, 0, i);
                os.flush();
            }
            response.flushBuffer();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @PostMapping(value = "/abortAll")
    @ResponseBody
    public Response abortAll(@RequestBody ParamDTO paramDTO){
        AssemblyResponse<String> assemblyResponse = new AssemblyResponse();
        chunkService.abortAll(paramDTO.getUserId());
        return assemblyResponse.success(null);
    }


}
