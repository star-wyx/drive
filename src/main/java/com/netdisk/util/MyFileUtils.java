package com.netdisk.util;

import com.netdisk.config.FileProperties;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public final class MyFileUtils {

    @Autowired
    FileProperties fileProperties;

    public void createFolder(String dictionary) {
        Path path = Paths.get(fileProperties.getRootDir() + dictionary);
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFile(MultipartFile file, String dictionary) {
        File localFile = new File(fileProperties.getRootDir() + dictionary);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void icon(String contentType) {
        /**
         * folder-fill
         * filetype-xxx
         * file-earmark
         * film
         * music-note-beamed
         * file-earmark-zip-fill
         * file-earmark-arrow-down bt种子
         * image
         * .svg
         */
    }

    public static String getMD5(String str) {
        String slat = "&%5123***&&%%$$#@";
        String base = str + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    /**
     * @param srcPath     原图片地址
     * @param desPath     目标图片地址
     * @param desFileSize 指定图片大小,单位kb
     * @param accuracy    精度,递归压缩的比率,建议小于0.9
     * @return
     */
    public String commpressPicForScale(String srcPath, String desPath,
                                              long desFileSize, double accuracy) {
        File desFile = null;
        try {
            File srcFile = new File(srcPath);
            long srcFilesize = srcFile.length();
            System.out.println("原图片:" + srcPath + ",大小:" + srcFilesize / 1024 + "kb");
            //递归压缩,直到目标文件大小小于desFileSize
            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
            commpressPicCycle(desPath, desFileSize, accuracy);

            desFile = new File(desPath);
            System.out.println("目标图片:" + desPath + ",大小" + desFile.length() / 1024 + "kb");
            System.out.println("图片压缩完成!");
            byte[] bytes = FileUtils.readFileToByteArray(desFile);
            desFile.delete();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return desPath;
    }

    public void commpressPicCycle(String desPath, long desFileSize,
                                         double accuracy) throws IOException {
        File imgFile = new File(desPath);
        long fileSize = imgFile.length();
        //判断大小,如果小于500k,不压缩,如果大于等于500k,压缩
        if (fileSize <= desFileSize * 500) {
            return;
        }
        //计算宽高
        BufferedImage bim = ImageIO.read(imgFile);
        int imgWidth = bim.getWidth();
        int imgHeight = bim.getHeight();
        int desWidth = new BigDecimal(imgWidth).multiply(
                new BigDecimal(accuracy)).intValue();
        int desHeight = new BigDecimal(imgHeight).multiply(
                new BigDecimal(accuracy)).intValue();
        Thumbnails.of(desPath).size(desWidth, desHeight).outputQuality(accuracy).toFile(desPath);
        //如果不满足要求,递归直至满足小于1M的要求
        commpressPicCycle(desPath, desFileSize, accuracy);
    }

    /**
     * 文件转成base64字符串,根据最大尺寸压缩
     */
    public String encodeFileToBase64BinaryWithImageSize(String storePath, long maxSize){
        File file = new File(fileProperties.getRootDir() + storePath);
        long fileSize = file.length();
        double accuracy = 1f;
        if (fileSize > maxSize) {
            accuracy = BigDecimal.valueOf(maxSize).divide(BigDecimal.valueOf(fileSize), 2, BigDecimal
                    .ROUND_DOWN).doubleValue();
        }
        try {
            return imgScaleOutputStream(file, accuracy);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片质量压缩
     *
     * @param file
     * @param accuracy
     * @return
     * @throws IOException
     */
    public String imgScaleOutputStream(File file, double accuracy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(file).scale(1f).outputQuality(0).toOutputStream(baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }


    public static String getMimeType(File file){
        InputStream is = null;
        String res = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            res =  URLConnection.guessContentTypeFromStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

}
