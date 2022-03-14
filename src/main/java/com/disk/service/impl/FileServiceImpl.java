package com.disk.service.impl;

import com.disk.repository.FileRepository;
import com.disk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Override
    public int uploadFile(Map<String,Object> map) {
        return fileRepository.uploadFile(map);
    }
}
