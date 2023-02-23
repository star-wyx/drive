package com.netdisk.util;

import com.netdisk.config.CodecProperties;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.*;
import org.bson.types.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class FfmpegUtil {

    @Autowired
    FileProperties fileProperties;

    @Autowired
    CodecProperties codecProperties;

    /**
     * 转换任意格式的视频为 mp4 格式的视频
     *
     * @param src  源视频文件
     * @param dest 保存 mp4 视频的文件
     * @throws IOException
     */
    public void convertToMp4(File src, File dest) throws IOException {
        FFmpeg ffmpeg = new FFmpeg(fileProperties.getFfmpegPath());
        FFprobe ffprobe = new FFprobe(fileProperties.getFfprobePath());
        FFmpegProbeResult in = ffprobe.probe(src.getAbsolutePath());

        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true) // Override the output if it exists
                .setInput(in)
                .addOutput(dest.getAbsolutePath())
                .setFormat("mp4")                  // Format is inferred from filename, or can be set
                .setVideoCodec("libx264")          // Video using x264
                .setVideoFrameRate(24, 1)          // At 24 frames per second
                // .setVideoResolution(width, height) // At 1280x720 resolution (宽高必须都能被 2 整除)
                .setAudioCodec("aac")              // Using the aac codec
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs (ex. aac)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            // 使用 FFmpegProbeResult 得到视频的长度 (单位为纳秒)
            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {
                // 转换进度 [0, 100]
                // [Fix] No duration for FLV, SWF file, 所以获取进度无效时都假装转换到了 99%
                int percentage = (duration_ns > 0) ? (int) (progress.out_time_ns / duration_ns * 100) : 99;

                // 日志中输出转换进度信息
                log.debug("[{}%] status: {}, frame: {}, time: {} ms, fps: {}, speed: {}x",
                        percentage,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        progress.fps.doubleValue(),
                        progress.speed
                );
            }
        });

        job.run();
    }

    /**
     * /usr/local/ffmpeg/bin/ffmpeg
     * {-hwaccel qsv -c:v (hevc_qsv)}
     * -i
     * /volume1/homes/wzl778633/2.mkv
     * -c:v h264_qsv -global_quality 25
     * /volume1/homes/wzl778633/1.mp4
     * <p>
     * <p>
     * hevc ==> -hwaccel qsv -c:v hevc_qsv
     * vp9 ==> -hwaccel qsv -c:v vp9_qsv
     * vp8 ==> -hwaccel qsv -c:v vp8_qsv
     * h264 ==> -hwaccel qsv -c:v h264_qsv
     * others ==>  -hwaccel qsv
     */


    public void convert(String src, String dest, String codec) {
        List<String> command = new ArrayList<>();

        log.info("TRANSCODE");

        command.add(fileProperties.getFfmpegPath());
        command.add("-hide_banner");
        if (codecProperties.getDecoder().get("hevc_qsv") || codecProperties.getDecoder().get("vp9_qsv")
                || codecProperties.getDecoder().get("vp8_qsv") || codecProperties.getDecoder().get("h264_qsv")
        || codecProperties.getEncoder().get("h264_qsv")) {
            command.add("-hwaccel");
            command.add("qsv");
        }
//        command.add("videotoolbox");
        if (codec.equals("hevc") && codecProperties.getDecoder().get("hevc_qsv")) {
            command.add("-c:v");
            command.add("hevc_qsv");
        } else if (codec.equals("vp9") && codecProperties.getDecoder().get("vp9_qsv")) {
            command.add("-c:v");
            command.add("vp9_qsv");
        } else if (codec.equals("vp8") && codecProperties.getDecoder().get("vp8_qsv")) {
            command.add("-c:v");
            command.add("vp8_qsv");
        } else if (codec.equals("h264") && codecProperties.getDecoder().get("h264_qsv")) {
            command.add("-c:v");
            command.add("h264_qsv");
        }
        command.add("-y");
        command.add("-i");
        command.add(src);

        if (codecProperties.getEncoder().get("h264_qsv")) {
            command.add("-c:v");
            command.add("h264_qsv");
//            command.add("h264_videotoolbox");
        }
        command.add("-global_quality");
        command.add("25");
        command.add(dest);

        log.info("执行命令" + command.toString());

        runProcess(command);

    }

    public void checkEncoders() {
        log.info("开始检查decoder");

        List<String> command = new ArrayList<>();
//        ffmpeg -dncoders|grep qsv
        command.add("ffmpeg");
        command.add("-hide_banner");
        command.add("-decoders");
        command.add("|grep");
        command.add("qsv");

        log.info("正在检查");

        String res = runProcess(command);
        if (res.contains("h264_qsv")) {
            codecProperties.getDecoder().put("h264_qsv", true);
            log.info("有h264_qsv");

        }
        if (res.contains("hevc_qsv")) {
            codecProperties.getDecoder().put("hevc_qsv", true);
            log.info("有hevc_qsv");

        }
        if (res.contains("vp8_qsv")) {
            codecProperties.getDecoder().put("vp8_qsv", true);
            log.info("有vp8_qsv");
        }
        if (res.contains("vp9_qsv")) {
            codecProperties.getDecoder().put("vp9_qsv", true);
            log.info("有vp9_qsv");
        }

        log.info("检查encoder");
        command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-hide_banner");
        command.add("-encoders");
        command.add("|grep");
        command.add("qsv");
//       command.add("videotoolbox");
        String encoderRes = runProcess(command);
        if (encoderRes.contains("h264_qsv")) {
            codecProperties.getEncoder().put("h264_qsv", true);
            log.info("有h264_qsv encoder");
        }

    }

    public String runProcess(List<String> command) {
        StringBuffer sb = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.redirectErrorStream(true);
            Process p = builder.start();

            //1. start
            BufferedReader buf = null; // 保存ffmpeg的输出结果流
            String line = null;
            //read the standard output

            buf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            sb = new StringBuffer();
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
                sb.append(line);
                continue;
            }

            int ret = p.waitFor();//这里线程阻塞，将等待外部转换进程运行成功运行结束后，才往下执行
            //1. end
        } catch (Exception e) {
            System.out.println(e);
        }
        return sb.toString();
    }


    public String getVideoFormat(String filePath) {
        Map<String, String> map = getEncodingFormat(filePath);
        return map.get("Video");
    }


    public Map<String, String> getEncodingFormat(String filePath) {
        String processFLVResult = processFLV(filePath);
        Map retMap = new HashMap();
        if (StringUtils.isNotBlank(processFLVResult)) {
            PatternCompiler compiler = new Perl5Compiler();
            try {
                String regexVideo = "Video: (.*?), (.*?), (.*?)[,\\s]";
                String regexAudio = "Audio: (\\w*), (\\d*) Hz";

                Pattern patternVideo = compiler.compile(regexVideo, Perl5Compiler.CASE_INSENSITIVE_MASK);
                PatternMatcher matcherVideo = new Perl5Matcher();

                if (matcherVideo.contains(processFLVResult, patternVideo)) {
                    MatchResult re = matcherVideo.getMatch();
                    String video = re.group(1);
                    if (video.contains("(")) {
                        video = video.substring(0, video.indexOf("(") - 1);
                    }
                    retMap.put("Video", video);

//                    retMap.put("视频格式", re.group(2));
//                    retMap.put("分辨率", re.group(3));
                    System.out.println("Video: " + video);
//                    System.out.println("视频格式 ===" + re.group(2));
//                    System.out.println(" 分辨率  == =" + re.group(3));
                }

                Pattern patternAudio = compiler.compile(regexAudio, Perl5Compiler.CASE_INSENSITIVE_MASK);
                PatternMatcher matcherAudio = new Perl5Matcher();

                if (matcherAudio.contains(processFLVResult, patternAudio)) {
                    MatchResult re = matcherAudio.getMatch();
                    retMap.put("Audio", re.group(1));
//                    retMap.put("音频采样频率", re.group(2));
                    System.out.println("Audio: " + re.group(1));
//                    System.out.println("音频采样频率  ===" + re.group(2));
                }
            } catch (MalformedPatternException e) {
                e.printStackTrace();
            }
        }
        return retMap;

    }

    //  获取ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
    private String processFLV(String inputPath) {
        List<String> commend = new java.util.ArrayList<String>();

        commend.add(fileProperties.getFfmpegPath());//可以设置环境变量从而省去这行
        commend.add("ffmpeg");
        commend.add("-i");
        commend.add(inputPath);

        try {

            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            builder.redirectErrorStream(true);
            Process p = builder.start();

            //1. start
            BufferedReader buf = null; // 保存ffmpeg的输出结果流
            String line = null;
            //read the standard output

            buf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            StringBuffer sb = new StringBuffer();
            while ((line = buf.readLine()) != null) {
//                System.out.println(line);
                sb.append(line);
                continue;
            }
            int ret = p.waitFor();//这里线程阻塞，将等待外部转换进程运行成功运行结束后，才往下执行
            //1. end
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }


}
