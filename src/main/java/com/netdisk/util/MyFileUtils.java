package com.netdisk.util;

import com.netdisk.config.FileProperties;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
//import java.util.regex.Pattern;
import org.apache.oro.text.regex.*;

@Component
@Slf4j
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

    public static String getMD5(File file) {
        try {
            return DigestUtils.md5DigestAsHex(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMd5ByStream(File file) {
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int len;
            while ((len = stream.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
            return toHexString(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPartMd5ByStream(File file) {
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            //lenght 为 3mb 的窗格
            int length = fileProperties.getSliceSizeMB() * 1024 * 1024;
            //王氏跳跃式超级md5算法 版权所有！
            //获得最大chunk数 向上取整 *1.0就是变成double的意思 最后一个chunk一定小于等于3mb
            int chunkNum = (int) Math.ceil(file.length() * 1.0 / length);
            //跳跃窗格 总共取100个chunk 每隔固定的数量的分块就取一个分块进行计算 总共取100+1个 这个固定的数量就是就是skipInterval
            //向下取整，意味着如果不足100个时 会变成0 即不跳跃
            int skipInterval = (int) Math.floor(chunkNum * 1.0 / 100);

            //debug参数 可无视
            int rightnow = 0;
            int counter = 0;

            //100+1个包 最后一个一定为结尾的最后一个包 以保证封闭完整性
            int maxAccount = 100;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //窗口定义
            byte[] buf = new byte[length];

            //是否超过了我们的限制？注意 边界100划分到全读到条件中！
            if (maxAccount < chunkNum) {
                while (maxAccount > 0) {
                    //debug 显示
                    System.out.println(rightnow + "  " + counter);
                    stream.read(buf);
                    digest.update(buf, 0, length);
                    //关键 跳跃包 如果间隔为0 就不跳跃，跳跃是根据上个包的终点跳n-1个包 （因为skipInterval为要的包和要跳的包的总和）
                    stream.skip((skipInterval == 0 ? skipInterval : skipInterval - 1) * length);
                    //debug 显示
                    rightnow = rightnow + skipInterval;
                    counter++;
                    //下一个
                    maxAccount--;
                }

                //最后的结尾包 因为取整的缘故 一定会剩余大于间隔的包数
                if (maxAccount == 0) {
                    //剩余包数
                    int availableChunk = (int) Math.ceil(stream.available() * 1.0 / length);
                    //debug显示
                    System.out.println((rightnow + availableChunk - 1) + "  " + counter++);
                    //跳掉最后一个包之前的包
                    stream.skip((availableChunk - 1) * length);
                    //注意！最后一个包长度不固定 必须另外取
                    int len = stream.read(buf);
                    digest.update(buf, 0, len);
                }
            } else {
                //你懂的 这部分是个人都看得懂
                for (int i = 0; i < chunkNum; i++) {
                    int len = stream.read(buf);
                    digest.update(buf, 0, len);
                }
            }


            return toHexString(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toHexString(byte[] data) {
        char[] hexCode = "0123456789ABCDEF".toCharArray();
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
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
//            long srcFilesize = srcFile.length();
//            System.out.println("原图片:" + srcPath + ",大小:" + srcFilesize / 1024 + "kb");
//            //递归压缩,直到目标文件大小小于desFileSize
//            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
//            commpressPicCycle(desPath, desFileSize, accuracy);
//
//            desFile = new File(desPath);
//            System.out.println("目标图片:" + desPath + ",大小" + desFile.length() / 1024 + "kb");
//            System.out.println("图片压缩完成!");
//            byte[] bytes = FileUtils.readFileToByteArray(desFile);
//            desFile.delete();
//            return Base64.getEncoder().encodeToString(bytes);
            return resizeImageTo50K(getBase64(srcFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String resizeImageTo50K(String base64Img) {
        try {
            BufferedImage src = base64String2BufferedImage(base64Img);
            BufferedImage output = Thumbnails.of(src).size(src.getWidth() / 5, src.getHeight() / 5).asBufferedImage();
            String base64 = imageToBase64(output);
            if (base64.length() - base64.length() / 8 * 2 > 200000) {
                output = Thumbnails.of(output).scale(1f).asBufferedImage();
                base64 = imageToBase64(output);
            }
            System.out.println("压缩后"+imageSize(base64));
            return base64;
        } catch (Exception e) {
            return base64Img;
        }
    }

    public static Integer imageSize(String imageBase64Str){

        //1.找到等号，把等号也去掉(=用来填充base64字符串长度用)
        Integer equalIndex= imageBase64Str.indexOf("=");
        if(imageBase64Str.indexOf("=")>0) {
            imageBase64Str=imageBase64Str.substring(0, equalIndex);
        }
        //2.原来的字符流大小，单位为字节
        Integer strLength=imageBase64Str.length();
        System.out.println("imageBase64Str Length = "+strLength);
        //3.计算后得到的文件流大小，单位为字节
        Integer size=strLength-(strLength/8)*2;
        return size;
    }

    public static String imageToBase64(BufferedImage bufferedImage) {
        Base64.Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e) {
        }
        return new String(encoder.encode((baos.toByteArray())));
    }

    /**
     * 将base64 转为流
     * @param base64string
     * @return
     */
    public static BufferedImage base64String2BufferedImage(String base64string) {
        BufferedImage image = null;
        try {
            InputStream stream = BaseToInputStream(base64string);
            image = ImageIO.read(stream);
        } catch (IOException e) {
        }
        return image;
    }

    private static InputStream BaseToInputStream(String base64string) {
        ByteArrayInputStream stream = null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes1 = decoder.decode(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return stream;
    }





    public String getBase64(File file) {
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        InputStream file1 = null;
        try {
            file1 = new FileInputStream(file);

            byte[] byteBuf = new byte[3 * 1024 * 1024];
            byte[] base64ByteBuf;
            int count1; //每次从文件中读取到的有效字节数
            while ((count1 = file1.read(byteBuf)) != -1) {
                if (count1 != byteBuf.length) {//如果有效字节数不为3*1000，则说明文件已经读到尾了，不够填充满byteBuf了
                    byte[] copy = Arrays.copyOf(byteBuf, count1); //从byteBuf中截取包含有效字节数的字节段
                    base64ByteBuf = Base64.getEncoder().encode(copy); //对有效字节段进行编码
                } else {
                    base64ByteBuf = Base64.getEncoder().encode(byteBuf);
                }
                os1.write(base64ByteBuf, 0, base64ByteBuf.length);
                os1.flush();
            }
            file1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return os1.toString();
    }

    public String getBase64(byte[] bytes) {
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(bytes);

            byte[] byteBuf = new byte[3 * 1024 * 1024];
            byte[] base64ByteBuf;
            int count1; //每次从文件中读取到的有效字节数
            while ((count1 = is.read(byteBuf)) != -1) {
                if (count1 != byteBuf.length) {//如果有效字节数不为3*1000，则说明文件已经读到尾了，不够填充满byteBuf了
                    byte[] copy = Arrays.copyOf(byteBuf, count1); //从byteBuf中截取包含有效字节数的字节段
                    base64ByteBuf = Base64.getEncoder().encode(copy); //对有效字节段进行编码
                } else {
                    base64ByteBuf = Base64.getEncoder().encode(byteBuf);
                }
                os1.write(base64ByteBuf, 0, base64ByteBuf.length);
                os1.flush();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return os1.toString();
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
    public String encodeFileToBase64BinaryWithImageSize(String storePath, long maxSize) {
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


    public static String getMimeType(File file) {
        InputStream is = null;
        String res = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            res = URLConnection.guessContentTypeFromStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


    public String getPrintSize(Long size) {

        long TB = 1024 * 1024 * 1024 * 1024L;//定义TB的计算常量
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        if (size == null) {
            return null;
        }
        try {
            // 格式化小数
            DecimalFormat df = new DecimalFormat("0.00");
            String resultSize = "";
            if (size / TB >= 1) {

                //如果当前Byte的值大于等于1TB
                resultSize = df.format(size / (float) TB) + "TB";
            } else if (size / GB >= 1) {

                //如果当前Byte的值大于等于1GB
                resultSize = df.format(size / (float) GB) + "GB";
            } else if (size / MB >= 1) {

                //如果当前Byte的值大于等于1MB
                resultSize = df.format(size / (float) MB) + "MB";
            } else if (size / KB >= 1) {

                //如果当前Byte的值大于等于1KB
                resultSize = df.format(size / (float) KB) + "KB";
            } else {

                resultSize = size + "B";
            }
            return resultSize;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getRatio(long film, long music, long picture, long others, long remain) {
        List<Long> longs = new ArrayList<>();
        longs.add(picture);
        longs.add(film);
        longs.add(music);
        longs.add(others);
        longs.add(remain);
        return getPercentValue(longs, 2);
    }

    public List<String> getPercentValue(List<Long> list, int precision) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        double listSum = list.stream().mapToDouble(p -> p).sum();
        if (listSum == 0) {
            return null;
        }
        List<Double> seatsList = new ArrayList<>();//整数值
        List<Double> votesPerQuotaList = new ArrayList<>();//求小数得集合
        double currentSum = 0;
        //10得二次幂是100用于计算精度
        double targetSeats = Math.pow(10, precision) * 100;
        for (long val : list) {
            //扩大比例100 用于计算
            //double result = divideToDouble((val * targetSeats),listSum);
            double result = val / listSum * targetSeats;
            double seats = Math.floor(result);
            currentSum = add(currentSum, seats);//求总和
            seatsList.add(seats);//取整数位
            votesPerQuotaList.add(subtract(result, seats));//取小数位
        }
        //给最大得值加1 凑够占比100%
        while (currentSum < targetSeats) {
            double max = 0;
            int maxId = 0;
            for (int i = 0; i < votesPerQuotaList.size(); i++) {
                if (votesPerQuotaList.get(i) > max) {
                    max = votesPerQuotaList.get(i);
                    maxId = i;
                }
            }
            //最大值加1 凑100
            seatsList.set(maxId, add(seatsList.get(maxId), 1));
            votesPerQuotaList.set(maxId, 0.0);//最大值小数位设为0
            currentSum = add(currentSum, 1);
        }

        List<String> res = new ArrayList<>();
        double tmp = 0;
        for (int i = 0; i < seatsList.size() - 1; i++) {
            String percentage = calculatePercentage(seatsList.get(i), targetSeats);
            if (percentage.equals("0") && list.get(i) != 0) {
                percentage = "0.01";
            }
            tmp += Double.parseDouble(percentage);
            res.add(percentage);
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        res.add(decimalFormat.format(100 - tmp));
        return res;

    }

    private static String calculatePercentage(double num1, double num2) {
        if (num1 == 0 || num2 == 0) {
            return "0";
        }
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(num1 / num2 * 100) + "";
    }

    private static double divideToDouble(double num1, int num2) {
        if (num1 == 0 || num2 == 0) {
            return 0.0f;
        }
        BigDecimal b1 = new BigDecimal(num1);
        BigDecimal b2 = new BigDecimal(num2);
        return b1.divide(b2, 8, BigDecimal.ROUND_DOWN).doubleValue();
    }

    private static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    private static double subtract(double num1, double num2) {
        BigDecimal b1 = new BigDecimal(num1);
        BigDecimal b2 = new BigDecimal(num2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 修改原图的文件格式
     *
     * @param srcPath    原图路径
     * @param destPath   新图路径
     * @param formatName 图片格式，支持bmp|gif|jpg|jpeg|png
     * @return
     */
    public static boolean modifyImageFormat(String srcPath, String destPath, String formatName) {
        boolean isSuccess = false;
        InputStream fis = null;
        try {
            fis = new FileInputStream(srcPath);
            BufferedImage bufferedImg = ImageIO.read(fis);
            isSuccess = ImageIO.write(bufferedImg, formatName, new File(destPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    public String getTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formatter.format(date);
    }
}
