package com.disk.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<T> {
    private int code;
    private T data;
    private boolean status;
}
