package com.netdisk.util;

import com.netdisk.config.FileProperties;
import com.netdisk.module.DTO.FileDTO;
import com.netdisk.module.FileNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component
public class TypeComparator<T> implements Comparator<T> {

    @Autowired
    FileProperties fileProperties;

    @Override
    public int compare(T o1, T o2) {
        FileDTO f1 = (FileDTO) o1;
        FileDTO f2 = (FileDTO) o2;
        int rank = ContentTypeRank(f1.getContentType(), f2.getContentType());
        if (rank != 0) {
            return rank;
        }
        if (f1.getContentType().equals(f2.getContentType())) {
            return f1.getFileName().compareTo(f2.getFileName());
        }else{
            return f1.getContentType().compareTo(f2.getContentType());
        }
    }

    public int ContentTypeRank(String type1, String type2) {
        Map<String, Integer> map = new HashMap<>();
        map.put(fileProperties.getIcon().get("folder"), 5);
        map.put(fileProperties.getIcon().get("film"), 4);
        map.put(fileProperties.getIcon().get("music"), 3);
        map.put(fileProperties.getIcon().get("picture"), 2);
        map.put("file-earmark", 0);

        return map.getOrDefault(type2, 1) - map.getOrDefault(type1, 1);

    }
}
