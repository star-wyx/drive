drop database netdisk;

CREATE database `netdisk`;

USE `netdisk`;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`(
    `user_id` int PRIMARY KEY AUTO_INCREMENT COMMENT '用户id',
    `user_name` char(20) NOT NULL COMMENT '用户名',
    `user_pwd` char(20) NOT NULL COMMENT '用户密码',
    `user_email` char(20) NOT NULL COMMENT '用户邮箱'
) ENGINE = INNODB DEFAULT CHAR SET=utf8;

Insert into netdisk.users values(null,'admin','admin','admin@disk.com');

drop table if exists `dir`;
create table `dir`(
    `dir_id` int primary key auto_increment comment '文件夹id',
    `dir_name` char(100) not null comment '文件夹名称',
    `parent_id` int not null comment '父文件夹id',
    `user_id` int not null comment '用户id',
    `dir_path` char(200) not null comment '网盘路径',
    `store_path` char(200) not null comment '存储路径',
    foreign key (user_id) references users(user_id)
);

insert dir values (null,'root',0,1,'/','/Users/star_wyx/Desktop/File');


drop table if exists `file`;
create table `file`(
    `id` int(10) primary key auto_increment comment'文件id',
    `file_name` char(100) not null comment '文件名称',
    `file_path` char(200) not null comment '文件路径',
    `store_path` char(200) not null comment '存储路径',
    `upload_time` timestamp not null comment '上传时间',
    `uid` int not null comment '上传用户的id',
    `dir_id` int not null comment '所属文件夹id',
    foreign key (uid) references users(user_id),
    foreign key (dir_id) references dir(dir_id)
);