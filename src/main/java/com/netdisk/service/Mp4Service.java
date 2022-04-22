package com.netdisk.service;

import com.netdisk.module.Mp4;

public interface Mp4Service {

    Mp4 queryByMd5(String md5);

    Mp4 queryByOtherMd5(String otherMd5);

    void add(String fileName, String md5, String storePath, String otherMd5);

}
