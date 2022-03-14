CREATE database `netdisk`;

USE `netdisk`;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`(
    `user_id` int PRIMARY KEY AUTO_INCREMENT COMMENT '用户id',
    `user_name` char(20) NOT NULL COMMENT '用户名',
    `user_pwd` char(20) NOT NULL COMMENT '用户密码',
    `user_email` char(20) NOT NULL COMMENT '用户邮箱'
) ENGINE = INNODB DEFAULT CHAR SET=utf8;

Insert into netdisk.users values(null,'张三','123','123@qq.com');

DROP TABLE IF EXISTS `users_data`;
CREATE TABLE `users_data`(
    `data_id` int primary key auto_increment comment '数据id',
    `user_id` int not NULL comment '数据所属用户的id',
    `content` char(200) NOT NULL comment '数据内容',
    foreign key (user_id) references users(user_id)
);

insert into users_data values(null,1,'I am Zhangsan');
insert into users_data values(null,2,'I am Mike');
insert into users_data values(null,4,'I am Alice');
insert into users_data values(null,8,'I am trump');
insert into users_data values(null,8,'I win 2024');


drop table if exists `file`;
create table `file`(
    `id` int(10) primary key auto_increment comment'文件id',
    `file_name` char(100) not null comment '文件名称',
    `file_path` char(100) not null comment '文件路径',
    `upload_time` char(100) not null comment '上传时间',
    `uid` int not null comment '上传用户的id',
    foreign key (uid) references users(user_id)
);