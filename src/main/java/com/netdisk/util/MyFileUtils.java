package com.netdisk.util;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.RoomDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.User;
import com.netdisk.module.chat.Room;
import com.netdisk.module.chat.RoomUser;
import com.netdisk.service.impl.FileServiceImpl;
import com.netdisk.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

@Component
@Slf4j
public final class MyFileUtils {

    @Autowired
    FileProperties fileProperties;

    @Autowired
    MongoTemplate mongoTemplate;

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
         * file-earmark-arrow-down bt??????
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
            //lenght ??? 3mb ?????????
            int length = fileProperties.getSliceSizeMB() * 1024 * 1024;
            //?????????????????????md5?????? ???????????????
            //????????????chunk??? ???????????? *1.0????????????double????????? ????????????chunk??????????????????3mb
            int chunkNum = (int) Math.ceil(file.length() * 1.0 / length);
            //???????????? ?????????100???chunk ???????????????????????????????????????????????????????????? ?????????100+1??? ?????????????????????????????????skipInterval
            //????????????????????????????????????100?????? ?????????0 ????????????
            int skipInterval = (int) Math.floor(chunkNum * 1.0 / 100);

            //debug?????? ?????????
            int rightnow = 0;
            int counter = 0;

            //100+1?????? ????????????????????????????????????????????? ????????????????????????
            int maxAccount = 100;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //????????????
            byte[] buf = new byte[length];

            //??????????????????????????????????????? ??????100??????????????????????????????
            if (maxAccount < chunkNum) {
                while (maxAccount > 0) {
                    //debug ??????
                    System.out.println(rightnow + "  " + counter);
                    stream.read(buf);
                    digest.update(buf, 0, length);
                    //?????? ????????? ???????????????0 ???????????????????????????????????????????????????n-1?????? ?????????skipInterval???????????????????????????????????????
                    stream.skip((skipInterval == 0 ? skipInterval : skipInterval - 1) * length);
                    //debug ??????
                    rightnow = rightnow + skipInterval;
                    counter++;
                    //?????????
                    maxAccount--;
                }

                //?????????????????? ????????????????????? ????????????????????????????????????
                if (maxAccount == 0) {
                    //????????????
                    int availableChunk = (int) Math.ceil(stream.available() * 1.0 / length);
                    //debug??????
                    System.out.println((rightnow + availableChunk - 1) + "  " + counter++);
                    //?????????????????????????????????
                    stream.skip((availableChunk - 1) * length);
                    //??????????????????????????????????????? ???????????????
                    int len = stream.read(buf);
                    digest.update(buf, 0, len);
                }
            } else {
                //????????? ??????????????????????????????
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
     * @param srcPath     ???????????????
     * @param desPath     ??????????????????
     * @param desFileSize ??????????????????,??????kb
     * @param accuracy    ??????,?????????????????????,????????????0.9
     * @return
     */
    public String commpressPicForScale(String srcPath, String desPath,
                                       long desFileSize, double accuracy) {
        File desFile = null;
        try {
            File srcFile = new File(srcPath);
//            long srcFilesize = srcFile.length();
//            System.out.println("?????????:" + srcPath + ",??????:" + srcFilesize / 1024 + "kb");
//            //????????????,??????????????????????????????desFileSize
//            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
//            commpressPicCycle(desPath, desFileSize, accuracy);
//
//            desFile = new File(desPath);
//            System.out.println("????????????:" + desPath + ",??????" + desFile.length() / 1024 + "kb");
//            System.out.println("??????????????????!");
//            byte[] bytes = FileUtils.readFileToByteArray(desFile);
//            desFile.delete();
//            return Base64.getEncoder().encodeToString(bytes);
            return resizeImageTo50K(getBase64(srcFile), desPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String resizeImageTo50K(String base64Img, String desFilePath) {
        try {
            BufferedImage src = base64String2BufferedImage(base64Img);
            int tmp = 0;
            if (src.getWidth() > 350 && src.getWidth() > src.getHeight()) {
                tmp = src.getWidth() / 350;
            } else if (src.getHeight() > 350 && src.getHeight() > src.getWidth()) {
                tmp = src.getWidth() / 350;
            }

            BufferedImage output = null;
            if (tmp != 0) {
                Thumbnails.of(src).size(src.getWidth() / 5, src.getHeight() / 5).toFile(desFilePath);
            } else {
                Thumbnails.of(src).toFile(desFilePath);
            }

            File desFile = new File(desFilePath);

            if (desFile.length() > 100 * 1024) {
                commpressPicCycle(desFilePath, 100, 0.7);
            }

            desFile = new File(desFilePath);
            String base64 = getBase64(desFile);
            desFile.delete();

//            if (base64.length() - base64.length() / 8 * 2 > 200000) {
//                output = Thumbnails.of(output).scale(1f).asBufferedImage();
//                base64 = imageToBase64(output);
//            }
            System.out.println("?????????" + imageSize(base64));
            return base64;
        } catch (Exception e) {
            return base64Img;
        }
    }

    public static Integer imageSize(String imageBase64Str) {

        //1.?????????????????????????????????(=????????????base64??????????????????)
        Integer equalIndex = imageBase64Str.indexOf("=");
        if (imageBase64Str.indexOf("=") > 0) {
            imageBase64Str = imageBase64Str.substring(0, equalIndex);
        }
        //2.??????????????????????????????????????????
        Integer strLength = imageBase64Str.length();
        System.out.println("imageBase64Str Length = " + strLength);
        //3.???????????????????????????????????????????????????
        Integer size = strLength - (strLength / 8) * 2;
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
     * ???base64 ?????????
     *
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
            System.out.println(e);
        }
        return stream;
    }


    public static String getBase64(File file) {
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        InputStream file1 = null;
        try {
            file1 = new FileInputStream(file);

            byte[] byteBuf = new byte[3 * 1024 * 1024];
            byte[] base64ByteBuf;
            int count1; //?????????????????????????????????????????????
            while ((count1 = file1.read(byteBuf)) != -1) {
                if (count1 != byteBuf.length) {//???????????????????????????3*1000??????????????????????????????????????????????????????byteBuf???
                    byte[] copy = Arrays.copyOf(byteBuf, count1); //???byteBuf??????????????????????????????????????????
                    base64ByteBuf = Base64.getEncoder().encode(copy); //??????????????????????????????
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
            int count1; //?????????????????????????????????????????????
            while ((count1 = is.read(byteBuf)) != -1) {
                if (count1 != byteBuf.length) {//???????????????????????????3*1000??????????????????????????????????????????????????????byteBuf???
                    byte[] copy = Arrays.copyOf(byteBuf, count1); //???byteBuf??????????????????????????????????????????
                    base64ByteBuf = Base64.getEncoder().encode(copy); //??????????????????????????????
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

    public static void commpressPicCycle(String desPath, long desFileSize,
                                         double accuracy) throws IOException {
        File imgFile = new File(desPath);
        long fileSize = imgFile.length();
        //????????????,????????????500k,?????????,??????????????????500k,??????
        if (fileSize <= desFileSize * 1024) {
            return;
        }
        //????????????
        BufferedImage bim = ImageIO.read(imgFile);
        int imgWidth = bim.getWidth();
        int imgHeight = bim.getHeight();
        int desWidth = new BigDecimal(imgWidth).multiply(
                new BigDecimal(accuracy)).intValue();
        int desHeight = new BigDecimal(imgHeight).multiply(
                new BigDecimal(accuracy)).intValue();
        Thumbnails.of(desPath).size(desWidth, desHeight).outputQuality(accuracy).toFile(desPath);
        //?????????????????????,????????????????????????1M?????????
        commpressPicCycle(desPath, desFileSize, accuracy);
    }

    /**
     * ????????????base64?????????,????????????????????????
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
     * ??????????????????
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

        long TB = 1024 * 1024 * 1024 * 1024L;//??????TB???????????????
        int GB = 1024 * 1024 * 1024;//??????GB???????????????
        int MB = 1024 * 1024;//??????MB???????????????
        int KB = 1024;//??????KB???????????????
        if (size == null) {
            return null;
        }
        try {
            // ???????????????
            DecimalFormat df = new DecimalFormat("0.00");
            String resultSize = "";
            if (size / TB >= 1) {

                //????????????Byte??????????????????1TB
                resultSize = df.format(size / (float) TB) + "TB";
            } else if (size / GB >= 1) {

                //????????????Byte??????????????????1GB
                resultSize = df.format(size / (float) GB) + "GB";
            } else if (size / MB >= 1) {

                //????????????Byte??????????????????1MB
                resultSize = df.format(size / (float) MB) + "MB";
            } else if (size / KB >= 1) {

                //????????????Byte??????????????????1KB
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
        List<Double> seatsList = new ArrayList<>();//?????????
        List<Double> votesPerQuotaList = new ArrayList<>();//??????????????????
        double currentSum = 0;
        //10???????????????100??????????????????
        double targetSeats = Math.pow(10, precision) * 100;
        for (long val : list) {
            //????????????100 ????????????
            //double result = divideToDouble((val * targetSeats),listSum);
            double result = val / listSum * targetSeats;
            double seats = Math.floor(result);
            currentSum = add(currentSum, seats);//?????????
            seatsList.add(seats);//????????????
            votesPerQuotaList.add(subtract(result, seats));//????????????
        }
        //??????????????????1 ????????????100%
        while (currentSum < targetSeats) {
            double max = 0;
            int maxId = 0;
            for (int i = 0; i < votesPerQuotaList.size(); i++) {
                if (votesPerQuotaList.get(i) > max) {
                    max = votesPerQuotaList.get(i);
                    maxId = i;
                }
            }
            //????????????1 ???100
            seatsList.set(maxId, add(seatsList.get(maxId), 1));
            votesPerQuotaList.set(maxId, 0.0);//????????????????????????0
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
     * ???????????????????????????
     *
     * @param srcPath    ????????????
     * @param destPath   ????????????
     * @param formatName ?????????????????????bmp|gif|jpg|jpeg|png
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

    public String availableFileName(Long userId, Long parentId, String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));
        StringBuilder sb = new StringBuilder();
        sb.append(fileName);
        int i = 1;
        while (true) {
            Query query = new Query(Criteria.where("userId").is(userId));
            query.addCriteria(Criteria.where("parentId").is(parentId));
            query.addCriteria(Criteria.where("fileName").is(sb.toString()));
            query.addCriteria(Criteria.where("isFolder").is(false));
            FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            if (fileNode == null) {
                break;
            } else {
                sb = new StringBuilder();
                sb.append(prefix).append(" (").append(i).append(")").append(suffix);
                i++;
            }
        }

        return sb.toString();
    }

    public String availableFolderName(Long userId, Long parentId, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileName);
        int i = 1;
        while (true) {
            Query query = new Query(Criteria.where("userId").is(userId));
            query.addCriteria(Criteria.where("parentId").is(parentId));
            query.addCriteria(Criteria.where("fileName").is(sb.toString()));
            query.addCriteria(Criteria.where("isFolder").is(true));
            FileNode fileNode = mongoTemplate.findOne(query, FileNode.class, FileServiceImpl.FILE_COLLECTION);
            if (fileNode == null) {
                break;
            } else {
                sb = new StringBuilder();
                sb.append(fileName).append(" (").append(i).append(")");
                i++;
            }
        }
        return sb.toString();
    }

    public List<Long> IntegerToLong(List<Integer> list) {
        List<Long> res = new ArrayList<>();
        for (Integer a : list) {
            res.add((long) a);
        }
        return res;
    }


}
