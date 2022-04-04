package com.netdisk.service;

public interface SeqService {

    long getNextSeqId(String collName);

    void insertUser(String userName);
}
