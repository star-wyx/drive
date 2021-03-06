package com.netdisk.service.impl;

import ch.qos.logback.core.util.FileUtil;
import com.netdisk.config.FileProperties;
import com.netdisk.module.Chunk;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Mp4;
import com.netdisk.module.User;
import com.netdisk.service.*;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ChunkServiceImpl implements ChunkService {

    public static final String CHUNK_COLLECTION = "chunk";

    public static final String UPLOADRECORD_COLLECTION = "UploadRecord";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileService fileService;

    @Autowired
    Mp4Service mp4Service;

    @Autowired
    UserService userService;

    @Autowired
    Md5Service md5Service;

    @Autowired
    MyFileUtils myFileUtils;


    @Override
    public Chunk queryByNameId(Long userId, Long nodeId, String fileName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("nodeId").is(nodeId));
        query.addCriteria(Criteria.where("fileName").is(fileName));
        return mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
    }

    @Override
    public void abortAll(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        List<Chunk> list = mongoTemplate.find(query, Chunk.class, CHUNK_COLLECTION);
        for (Chunk chunk : list) {
            try {
                FileUtils.deleteDirectory(new File(chunk.getStorePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mongoTemplate.findAllAndRemove(query, Chunk.class, CHUNK_COLLECTION);
    }

    @Override
    public Long createTask(String md5, String uuid, Long userId, Long nodeId, String fileName) {
        log.info(md5);
        Long res;
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        if (chunk == null) {
            String tmpPath = fileProperties.getTmpPath() + File.separator + uuid;
            File tmpFolder = new File(tmpPath);
            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }
            String availableName = myFileUtils.availableFileName(userId, nodeId, fileName);
            chunk = new Chunk(null, 0L, uuid, md5, userId, nodeId, availableName, tmpPath, new Date());
            mongoTemplate.save(chunk, CHUNK_COLLECTION);
            res = -1L;
        } else {
            res = chunk.getSerialNo();
        }
        return res;
    }

    @Override
    public int uploadSlice(MultipartFile file, String uuid, String sliceName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        Long serialNo = Long.valueOf(sliceName.substring(sliceName.lastIndexOf("_") + 1, sliceName.lastIndexOf(".")));
//        if (serialNo <= chunk.getSerialNo()) {
//            return 200;
//        } else if (serialNo == chunk.getSerialNo() + 1) {
//            File newFile = new File(chunk.getStorePath(), serialNo + ".tmp");
//            try {
//                file.transferTo(newFile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Update update = new Update();
//            update.set("serialNo", chunk.getSerialNo() + 1);
//            update.set("uploadTime", new Date());
//            mongoTemplate.updateFirst(query, update, Chunk.class, CHUNK_COLLECTION);
//            return 200;
//        } else {
//            return 454;
//        }

        //????????????
        File newFile = new File(chunk.getStorePath(), serialNo + ".tmp");
        try {
            file.transferTo(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Update update = new Update();
        update.set("serialNo", chunk.getSerialNo() < serialNo ? serialNo : chunk.getSerialNo());
        update.set("uploadTime", new Date());
        mongoTemplate.updateFirst(query, update, Chunk.class, CHUNK_COLLECTION);
        return 200;

    }

    @Override
    public int merge(String uuid, String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        assert chunk != null;
        FileNode folder = fileService.queryFolderById(chunk.getUserId(), chunk.getNodeId());
        if (folder == null) {
            abort(uuid, md5);
            return 453;
        }
        User user = userService.getUserById(chunk.getUserId());
//        String availableFileName = availableFileName(user, chunk.getNodeId(), chunk.getFileName());
//        File newFile = new File(fileProperties.getRootDir() + folder.getFilePath(), chunk.getFileName());
        String suffix = chunk.getFileName().substring(chunk.getFileName().lastIndexOf(".") + 1);
        suffix = suffix.toLowerCase(Locale.ROOT);
        File newFile = new File(fileProperties.getRootDir() + "/" + user.getUserName() + "/" + md5 + "." + suffix);
        byte[] byt = new byte[fileProperties.getSliceSizeMB() * 1024 * 1024];
        if (!newFile.exists()) {

            try {
                newFile.createNewFile();
                FileInputStream in = null;
                FileOutputStream out = new FileOutputStream(newFile, true);
                for (int i = 1; i <= chunk.getSerialNo(); i++) {
                    int len;
                    File slice = new File(chunk.getStorePath(), i + ".tmp");
                    if (!slice.exists()) {
                        abort(uuid, md5);
                        newFile.delete();
                        return 458;
                    }
                    in = new FileInputStream(new File(chunk.getStorePath(), i + ".tmp"));
                    while ((len = in.read(byt)) != -1) {
//                    System.out.println("------" + len);
                        out.write(byt, 0, len);
                    }
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return 500;
            }

        }

        try {
            FileUtils.deleteDirectory(new File(chunk.getStorePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String newFileMd5 = null;
        if (chunk.getSerialNo() <= 100) {
            newFileMd5 = myFileUtils.getPartMd5ByStream(newFile);
            log.info("getMd5ByStream new file md5: " + newFileMd5);
        } else {
            newFileMd5 = myFileUtils.getPartMd5ByStream(newFile);
            log.info("new file md5: " + newFileMd5);
        }

        if (newFileMd5 == null || !newFileMd5.toLowerCase(Locale.ROOT).equals(md5)) {
            System.out.println(newFileMd5);
            newFile.delete();
            mongoTemplate.remove(query, Chunk.class, CHUNK_COLLECTION);
            return 458;
        }

        // ????????????
        md5Service.increaseIndex(md5);

        userService.updateSize(user.getUserId(), newFile.length());
        fileService.insertFileNode(user, chunk.getNodeId(), chunk.getFileName(), md5, newFile.length());
        mongoTemplate.remove(query, Chunk.class, CHUNK_COLLECTION);
        return 200;
    }

    @Override
    public Long deleteChunk(String uuid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        return mongoTemplate.remove(query, Chunk.class, CHUNK_COLLECTION).getDeletedCount();
    }

    @Override
    public int abort(String uuid, String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        if (chunk == null) {
            return 453;
        }
        File file = new File(chunk.getStorePath());
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mongoTemplate.remove(chunk);
        }
        return 200;
    }

    /**
     * ??????????????????
     * * @param range http?????????Range?????????????????????????????????????????????
     * * ????????????Range: bytes=start-end  [start,end]?????????????????????????????????start???end???????????????
     * * @param request
     * * @param response
     */
    public void fileChunkDownload(String range, String filePath, HttpServletRequest request, HttpServletResponse response) {
        //????????????????????????????????????pom.xml??????????????????????????????????????????????????????????????????
        File file = new File(filePath);
        //??????????????????
        long startByte = 0;
        // ??????????????????
        long endByte = file.length() - 1;
        //???range??????
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //??????range?????????????????????????????????
                if (ranges.length == 1) {
                    //??????1?????????bytes=-1024  ?????????????????????1024??????????????????
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //??????2?????????bytes=1024-  ???1024?????????????????????????????????
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //??????3?????????bytes=1024-2048  ???1024????????????2048??????????????????
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
            }
        }
        //??????????????????
        long contentLength = endByte - startByte + 1;
        // ?????????
        String fileName = file.getName();
        //????????????
        String contentType = request.getServletContext().getMimeType(fileName);
        //???????????????
        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
        response.setHeader("Accept-Ranges", "bytes");
        //Content-Type ???????????????????????????????????????
        response.setHeader("Content-Type", contentType);
        //Content-Disposition ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ??????????????????????????????????????????????????????inline???????????????????????????????????????????????????
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        //Content-Length ?????????????????????????????????????????????
        response.setHeader("Content-Length", String.valueOf(contentLength));
        //Content-Range ??????????????????????????????????????????[????????????????????????]-[????????????]/[???????????????]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
        response.setStatus(response.SC_OK);
        response.setContentType(contentType);
        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        //?????????????????????
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int len = 0;
            randomAccessFile.seek(startByte);
            //??????????????????????????????2048???buff???length??????byte
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //????????????buff.length??????
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void vDownload(HttpServletRequest request, HttpServletResponse response) {
        String url = null;
        try {
            url = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String md5 = null;
        md5 = url.substring(url.lastIndexOf("vdownload/") + "vdownload".length() + 1);

        FileNode fileNode = fileService.checkMd5(md5);
        if (fileNode == null) {
            return;
        }
        String storePath = fileProperties.getRootDir() + fileNode.getStorePath();

        File file = new File(storePath);
        if (!file.exists()) {
            System.out.println("Not exist!");
            return;
        }

        String fileName = fileNode.getFileName();
        String contentType = request.getServletContext().getMimeType(file.getName());

        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setStatus(response.SC_OK);
            response.setHeader("Content-Length", String.valueOf(file.length()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Type", contentType);
//        response.setHeader("Content-Type", "application/octet-stream");

        response.setContentType(contentType);

        OutputStream os = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
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

    @Override
    public void vOpen(HttpServletRequest request, HttpServletResponse response) {
        String range = request.getHeader("Range");
        String url = null;
        try {
            url = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String md5 = null;
        md5 = url.substring(url.lastIndexOf("/") + 1);

        FileNode fileNode = fileService.checkMd5(md5);
        String storePath = null;
        String fileName = null;
        if (range == null) {
            vOpenPdf(fileNode, request, response);
            return;
        }

        if (fileNode == null) {
            Mp4 mp4 = mp4Service.queryByMd5(md5);
            mp4Service.updateTime(mp4.getMd5());
            storePath = mp4.getStorePath();
            fileName = mp4.getFileName();
        } else {
            storePath = fileProperties.getRootDir() + fileNode.getStorePath();
            fileName = fileNode.getFileName();
        }


        File file = new File(storePath);
        if (!file.exists()) {
            System.out.println("Not exist!");
            return;
        }

//        String contentType = request.getServletContext().getMimeType(file.getName());
        String contentType = MyFileUtils.getMimeType(file);


        long endByte = file.length() - 1;
        long startByte = 0L;
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //??????range?????????????????????????????????
                if (ranges.length == 1) {
                    //??????1?????????bytes=-1024  ?????????????????????1024??????????????????
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //??????2?????????bytes=1024-  ???1024?????????????????????????????????
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //??????3?????????bytes=1024-2048  ???1024????????????2048??????????????????
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
            }
        }

        long contentLength = endByte - startByte + 1;
        if (contentLength > 2097152) {
            contentLength = 2097152;
            endByte = contentLength + startByte - 1;
        }
        response.setHeader("Content-Length", String.valueOf(contentLength));
        try {
            response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setStatus(response.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", contentType);
//        response.setHeader("Content-Type", "application/octet-stream");
        response.setContentType(contentType);

        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        long transmitted = 0;

        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2097152];
            int len = 0;
            randomAccessFile.seek(startByte);
            len = randomAccessFile.read(buff);
            outputStream.write(buff, 0, len);
            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void vOpenPdf(FileNode fileNode, HttpServletRequest request, HttpServletResponse response) {
        log.info("==================enter vOpenPdf===================");
        String storePath = fileProperties.getRootDir() + fileNode.getStorePath();
        File file = new File(storePath);
        if (!file.exists()) {
            System.out.println("Not exist!");
            return;
        }

        String fileName = fileNode.getFileName();
        String contentType = request.getServletContext().getMimeType(file.getName());
        response.setHeader("Content-Type", contentType);
        response.setContentType(contentType);

        if (fileNode.getContentType().equals(fileProperties.getIcon().get("picture"))) {
            response.addHeader("Cache-Control", "max-age=15, must-revalidate");
            String ifNoneMatch = request.getHeader("If-None-Match");
            response.setHeader("Last-Modified", fileNode.getUploadTime());
            response.setHeader("ETag", fileNode.getMd5());
            if (ifNoneMatch != null && ifNoneMatch.equals(fileNode.getMd5())) {
                response.setStatus(response.SC_NOT_MODIFIED);
                System.out.println(response.getStatus());
                return;
            }
        }


        long endByte = file.length() - 1;
        long startByte = 0L;

        long contentLength = endByte - startByte + 1;
        response.setHeader("Content-Length", String.valueOf(contentLength));

        try {
            response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setStatus(response.SC_OK);
            response.setHeader("Content-Length", String.valueOf(file.length()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        OutputStream os = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
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

}