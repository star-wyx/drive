package com.netdisk.service;

public interface Md5Service {

    /**
     * 通过md5值读取信息
     */
    long getIndex(String md5);

    /**
     * 删除md5
     */
    void deleteMd5(String md5);

    /**
     * 增加index
     */
    void increaseIndex(String md5);

    /**
     * 将index减一，如果index为0，删除
     */
    long decreaseIndex(String md5);

}
