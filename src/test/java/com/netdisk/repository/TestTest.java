package com.netdisk.repository;

import com.netdisk.module.FileNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    NodeRepository nodeRepository;

    @Test
    public void getSubTree(){
        List<FileNode> list = nodeRepository.getSubTree(1L,1L,1L);
        for(FileNode fileNode:list){
            System.out.println(fileNode);
        }
    }

    @Test
    public void findDistinctByUserId(){
        System.out.println(nodeRepository.findDistinctByUserId(1L));
    }

    @Test
    public void findDistinctByUserIdAndNodeId(){
        System.out.println(nodeRepository.findDistinctByUserIdAndNodeId(1L,2L));
    }

    @Test
    public void test(){
        String[] strings = {"aac","ai","bmp","cs","css","csv","doc","docx","exe","gif","heic","html","java","jpg","js",
        "jpg","js","json","jsx","key","m4p","md","mdx","mov","mp3","mp4","otf","pdf","php","png","ppt","pptx","psd",
        "py","raw","rb","sass","scss","sh","svg","tiff","tsx","ttf","txt","wav","woff","xlx","xlsx","xml","yml"};
        for(String s: strings){
            System.out.println(s+": "+"filetype-"+s);
        }
    }

}