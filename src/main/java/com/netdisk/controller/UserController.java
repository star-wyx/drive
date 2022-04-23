package com.netdisk.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.ParamDTO;
import com.netdisk.module.DTO.UserDTO;
import com.netdisk.module.FileNode;
import com.netdisk.module.Profile;
import com.netdisk.module.User;
import com.netdisk.service.FileService;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.JwtUtil;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import io.swagger.annotations.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Api(value = "用户接口")
@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "http://192.168.1.169:9070", allowCredentials = "true")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SeqService seqService;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private MyFileUtils myFileUtils;

    /**
     * 用户登陆，成功则返回根目录
     *
     * @param userDTO user,用户名或邮箱
     *                user_pwd,密码
     * @return user_name, message
     */
    @PostMapping("/login")
    @ResponseBody
    public Response login(@RequestBody UserDTO userDTO) {
        Response res = userService.login(userDTO.getUser(), userDTO.getUserPwd());
        if (res.getCode() != 200) {
            return res;
        } else {
            User user = (User) res.getData();
            AssemblyResponse<ParamDTO> assembly = new AssemblyResponse<>();
            ParamDTO paramDTO = fileService.queryFolderRootContent(user);
            String token = JwtUtil.sign(user.getUserName(), user.getUserPwd());
            paramDTO.setToken(token);
            return assembly.success(paramDTO);
        }
    }

    /**
     * 用户注册
     *
     * @param userDTO user_name,用户名
     *                user_email,用户邮箱
     *                user_pwd,密码
     */
    @PostMapping("/signin")
    @ResponseBody
    public Response signin(@RequestBody UserDTO userDTO) {
        Response response = userService.add(userDTO);
        if (response.getCode() == 200) {
            fileService.createUserFile(userService.getUserByName(userDTO.getUserName()));
        }
        return response;
    }

    /**
     * 上传头像
     */
    @PostMapping("/updatePhoto")
    @ResponseBody
    public Response updatePhoto(@RequestParam("file") MultipartFile file,
                                @RequestParam("user_id") Long userId) {
        AssemblyResponse<Integer> assembly = new AssemblyResponse();
        if (userService.uploadPicture(file, userId) == 200) {
            return assembly.success(null);
        } else {
            return assembly.fail(400, null);
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePwd")
    @ResponseBody
    public Response changePwd(@RequestBody ParamDTO paramDTO) {
        int res = userService.updatePwd(paramDTO.getUserId(), paramDTO.getNewPwd(), paramDTO.getOldPwd());
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if (res == 200) {
            return assembly.success("password change successful");
        } else {
            return assembly.fail(res, "wrong password");
        }
    }

    /**
     * 查看用户详细信息
     * userId
     */
    @PostMapping("/profile")
    @ResponseBody
    public Response<Profile> profile(@RequestBody ParamDTO paramDTO) {
        long filmSize = 0L;
        long musicSize = 0L;
        long pictureSize = 0L;
        long others = 0L;
        long remain = 0L;
        User user = userService.getUserById(paramDTO.getUserId());
        List<FileNode> list = fileService.queryAllFiles(paramDTO.getUserId());
        for (FileNode fileNode : list) {
            if (!fileNode.isFolder()) {
                if (fileNode.getContentType().equals(fileProperties.getIcon().get("film"))) {
                    filmSize += fileNode.getFileSize();
                } else if (fileNode.getContentType().equals(fileProperties.getIcon().get("music"))) {
                    musicSize += fileNode.getFileSize();
                } else if (fileNode.getContentType().equals(fileProperties.getIcon().get("picture"))) {
                    pictureSize += fileNode.getFileSize();
                } else {
                    others += fileNode.getFileSize();
                }
            }
        }
        remain = userService.availableSpace(user.getUserId());
        Profile profile = new Profile(
                user.getUserEmail(),
                myFileUtils.getPrintSize(remain),
                myFileUtils.getPrintSize(filmSize),
                myFileUtils.getPrintSize(musicSize),
                myFileUtils.getPrintSize(pictureSize),
                myFileUtils.getPrintSize(others),
                user.getBase64(),
                myFileUtils.getRatio(filmSize, musicSize, pictureSize, others, remain)
        );
        AssemblyResponse<Profile> assembly = new AssemblyResponse<>();
        return assembly.success(profile);
    }

    @PostMapping("/checkToken")
    @ResponseBody
    public Response checkToken(@RequestBody ParamDTO paramDTO) {
        AssemblyResponse assembly = new AssemblyResponse();
        ParamDTO res = new ParamDTO();
        String token = paramDTO.getToken();
        DecodedJWT jwt = JWT.decode(token);
        String userName = jwt.getClaim("userName").asString();
        // 这边拿到的 用户名 应该去数据库查询获得密码，简略，步骤在service直接获取密码
        User user = userService.getUserByName(userName);
        boolean result = JwtUtil.verify(token, userName, user.getUserPwd());
        if (result) {
            res.setUserId(user.getUserId());
            res.setUserName(user.getUserName());
            return assembly.success(res);
        }else{
            return assembly.fail(456,null);
        }
    }

}
