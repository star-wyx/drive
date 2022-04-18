package com.netdisk.service.impl;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.UserDTO;
import com.netdisk.module.User;
import com.netdisk.service.SeqService;
import com.netdisk.service.UserService;
import com.netdisk.util.AssemblyResponse;
import com.netdisk.util.MyFileUtils;
import com.netdisk.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Service
public class UserServiceImpl implements UserService {

    public static final String USER_COLLECTION = "user";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SeqService seqService;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private MyFileUtils myFileUtils;

    @Override
    public Response<Object> login(String userName, String pwd) {
        User user;
        AssemblyResponse<Object> assembly = new AssemblyResponse<>();
        if (!userName.contains("@")) {
            user = getUserByName(userName);
            if (user == null) {
                return assembly.fail(402, "登陆用户名不存在");
            }
        } else {
            user = getUserByEmail(userName);
            if (user == null) {
                return assembly.fail(403, "登陆邮箱不存在");
            }
        }
        if (!user.getUserPwd().equals(pwd)) {
            return assembly.fail(401, "登陆密码错误");
        }
        return assembly.success(user);
    }

    @Override
    public Response add(UserDTO userDTO) {
        String userName = userDTO.getUserName() != null ? userDTO.getUserName() : userDTO.getUser();
        String userEmail = userDTO.getUserEmail() != null ? userDTO.getUserEmail() : userDTO.getUser();
        User byName = getUserByName(userName);
        User byEmail = getUserByEmail(userEmail);
        AssemblyResponse<String> assembly = new AssemblyResponse<>();
        if (byName == null && byEmail == null) {
            User user = userDTO.ToUser(seqService.getNextSeqId(USER_COLLECTION));
            mongoTemplate.save(user, USER_COLLECTION);
            seqService.insertUser(userDTO.getUserName());

            return assembly.success("注册成功");
        } else if (byName != null) {
            return assembly.fail(404, "用户名被占用");
        } else {
            return assembly.fail(405, "邮箱被占用");
        }
    }

    @Override
    public User getUserByName(String userName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userName").is(userName));
        return mongoTemplate.findOne(query, User.class, USER_COLLECTION);
    }

    @Override
    public User getUserByEmail(String userEmail) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userEmail").is(userEmail));
        return mongoTemplate.findOne(query, User.class, USER_COLLECTION);
    }

    @Override
    public User getUserById(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, User.class, USER_COLLECTION);
    }

    @Override
    public int uploadPicture(MultipartFile file, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        User user = mongoTemplate.findOne(query, User.class, USER_COLLECTION);
        File folder = new File(fileProperties.getProfileDir());
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        if(!folder.exists()){
            folder.mkdirs();
        }
        File picture = new File(folder,user.getUserName()+suffix);
        if(picture.exists()){
            picture.delete();
        }
        try {
            file.transferTo(picture);
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
        Update update = new Update();
        String desPath = fileProperties.getProfileDir() + "/" + user.getUserId() + suffix;
        update.set("base64", myFileUtils.commpressPicForScale(picture.getAbsolutePath(), desPath, 50, 0.7));
        mongoTemplate.findAndModify(query,update,User.class,USER_COLLECTION);
        return 200;
    }

    @Override
    public int updatePwd(Long userId, String newPwd, String oldPwd) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("userPwd").is(oldPwd));
        User user = mongoTemplate.findOne(query, User.class, USER_COLLECTION);
        if(user == null){
            return 401;
        }
        Update update = new Update();
        update.set("userPwd", newPwd);
        mongoTemplate.findAndModify(query,update,User.class);
        return 200;
    }
}
