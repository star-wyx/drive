package com.netdisk.service.impl;

import ch.qos.logback.core.util.FileUtil;
import com.netdisk.config.FileProperties;
import com.netdisk.module.Chunk;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.service.ChunkService;
import com.netdisk.service.FileService;
import com.netdisk.service.UserService;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Service
public class ChunkServiceImpl implements ChunkService {

    public static final String CHUNK_COLLECTION = "chunk";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Override
    public Long createTask(String md5, String uuid, Long userId, Long nodeId, String fileName) {
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
            chunk = new Chunk(null, 0L, uuid, md5, userId, nodeId, fileName, tmpPath);
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
        if (serialNo <= chunk.getSerialNo()) {
            return 200;
        } else if (serialNo == chunk.getSerialNo() + 1) {
            File newFile = new File(chunk.getStorePath(), serialNo + ".tmp");
            try {
                file.transferTo(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Update update = new Update();
            update.set("serialNo", chunk.getSerialNo() + 1);
            mongoTemplate.updateFirst(query, update, Chunk.class, CHUNK_COLLECTION);
            return 200;
        } else {
            return 454;
        }
    }

    @Override
    public int merge(String uuid, String md5) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        Chunk chunk = mongoTemplate.findOne(query, Chunk.class, CHUNK_COLLECTION);
        assert chunk != null;
        FileNode folder = fileService.queryFolderById(chunk.getUserId(), chunk.getNodeId());
        User user = userService.getUserById(chunk.getUserId());
        String availableFileName = fileService.availableFileName(user, chunk.getNodeId(), chunk.getFileName());
        File newFile = new File(fileProperties.getRootDir() + folder.getFilePath(), availableFileName);
        byte[] byt = new byte[fileProperties.getSliceSizeMB() * 1024 * 1024];
        try {
            newFile.createNewFile();
            FileInputStream in = null;
            FileOutputStream out = new FileOutputStream(newFile, true);
            for (int i = 1; i <= chunk.getSerialNo(); i++) {
                int len;
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

        try {
            FileUtils.deleteDirectory(new File(chunk.getStorePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileService.insertFileNode(user, chunk.getNodeId(), availableFileName);
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
        boolean bol = FileUtils.deleteQuietly(new File(chunk.getStorePath()));
        mongoTemplate.remove(chunk);
        return 200;
    }

    /**
     * 文件分片下载
     * * @param range http请求头Range，用于表示请求指定部分的内容。
     * * 格式为：Range: bytes=start-end  [start,end]表示，即是包含请求头的start及end字节的内容
     * * @param request
     * * @param response
     */
    public void fileChunkDownload(String range, String filePath, HttpServletRequest request, HttpServletResponse response) {
        //要下载的文件，此处以项目pom.xml文件举例说明。实际项目请根据实际业务场景获取
        File file = new File(filePath);
        //开始下载位置
        long startByte = 0;
        // 结束下载位置
        long endByte = file.length() - 1;
        //有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //根据range解析下载分片的位置区间
                if (ranges.length == 1) {
                    //情况1，如：bytes=-1024  从开始字节到第1024个字节的数据
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //情况2，如：bytes=1024-  第1024个字节到最后字节的数据
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //情况3，如：bytes=1024-2048  第1024个字节到2048个字节的数据
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
            }
        }
        //要下载的长度
        long contentLength = endByte - startByte + 1;
        // 文件名
        String fileName = file.getName();
        //文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        //响应头设置
        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
        response.setHeader("Accept-Ranges", "bytes");
        //Content-Type 表示资源类型，如：文件类型
        response.setHeader("Content-Type", contentType);
        //Content-Disposition 表示响应内容以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
        // 这里文件名换成下载后你想要的文件名，inline表示内联的形式，即：浏览器直接下载
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        //Content-Length 表示资源内容长度，即：文件大小
        response.setHeader("Content-Length", String.valueOf(contentLength));
        //Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
        response.setStatus(response.SC_OK);
        response.setContentType(contentType);
        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int len = 0;
            randomAccessFile.seek(startByte);
            //判断是否到了最后不足2048（buff的length）个byte
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //处理不足buff.length部分
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
            while ((i = bis.read(buff)) != 1) {
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
    public void vOpen(String range, HttpServletRequest request, HttpServletResponse response) {
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


        long endByte = file.length() - 1;
        long startByte = 0L;
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //根据range解析下载分片的位置区间
                if (ranges.length == 1) {
                    //情况1，如：bytes=-1024  从开始字节到第1024个字节的数据
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //情况2，如：bytes=1024-  第1024个字节到最后字节的数据
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //情况3，如：bytes=1024-2048  第1024个字节到2048个字节的数据
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
            byte[] buff = new byte[2048];
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

}