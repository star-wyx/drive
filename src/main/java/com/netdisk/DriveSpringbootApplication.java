package com.netdisk;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.netdisk.config.FileProperties;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
@EnableConfigurationProperties({
        FileProperties.class
})
public class DriveSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveSpringbootApplication.class, args);
    }


    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(64000);
        return loggingFilter;
    }


    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }

    /**
     * 注册netty-socketio服务端
     *
     * @return
     * @author liangxifeng 2018-07-07
     */
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

        String os = System.getProperty("os.name");
        if (os.equals("Mac OS X")) {   //在本地window环境测试时用localhost
            System.out.println("this is Mac OS X");
            config.setHostname("192.168.1.143");
        } else {
//            config.setHostname("172.17.0.1");
//            config.setHostname("aijiangsb.com");
//            config.setHostname("www.aijiangsb.com");
//            config.setHostname("192.168.5.130");
//            config.setHostname("127.0.0.1");
            config.setHostname("0.0.0.0");
        }
        config.setPort(27000);

        /*config.setAuthorizationListener(new AuthorizationListener() {//类似过滤器
            @Override
            public boolean isAuthorized(HandshakeData data) {
                //http://localhost:8081?username=test&password=test
                //例如果使用上面的链接进行connect，可以使用如下代码获取用户密码信息，本文不做身份验证
                // String username = data.getSingleUrlParam("username");
                // String password = data.getSingleUrlParam("password");
                return true;
            }
        });*/

        final SocketIOServer server = new SocketIOServer(config);
        return server;
    }

    /**
     * tomcat启动时候，扫码socket服务器并注册
     *
     * @param socketServer
     * @return
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }
}

