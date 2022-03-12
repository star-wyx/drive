CREATE database `netdisk`;

USE `netdisk`;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`(
    `user_id` int PRIMARY KEY AUTO_INCREMENT COMMENT '用户id',
    `user_name` char(20) NOT NULL COMMENT '用户名',
    `user_pwd` char(20) NOT NULL COMMENT '用户密码',
    `user_email` char(20) NOT NULL COMMENT '用户邮箱'
) ENGINE = INNODB DEFAULT CHAR SET=utf8

Insert into netdisk.users values(null,'张三','123','123@qq.com')