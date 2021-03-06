package com.netdisk.config;

import com.netdisk.module.User;
import com.netdisk.service.UserService;
import com.netdisk.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author lsc
 * <p>token验证拦截器 </p>
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 http 请求头中取出 token
        String token = request.getHeader("token");
        if(token == null){
            String url = request.getQueryString();
            log.info("url is: "+url);
            Pattern pattern = Pattern.compile("(token=)(.*)");
            Matcher matcher = pattern.matcher(url);
            matcher.find();
            token = matcher.group(2);
            if(token.contains("&time=")){
                token = token.substring(0,token.lastIndexOf("&time="));
            }
            if(token.contains("&fullfilename=")){
                token = token.substring(0,token.lastIndexOf("&fullfilename="));
            }
//            token = url.substring(url.lastIndexOf("=") + 1);
        }
        log.info("token is " + token);
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (token != null) {
            String userName = JwtUtil.getUserNameByToken(token);
            // 这边拿到的 用户名 应该去数据库查询获得密码，简略，步骤在service直接获取密码
            User user = userService.getUserByName(userName);
            if (user != null) {
                boolean result = JwtUtil.verify(token, userName, user.getUserPwd());
                if (result) {
                    System.out.println("通过拦截器");
                    return true;
                }
            }
        }
        response.setStatus(401);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}