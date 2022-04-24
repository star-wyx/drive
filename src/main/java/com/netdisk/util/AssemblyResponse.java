package com.netdisk.util;

public class AssemblyResponse<T> {

    private Response<T> response;

    public Response<T> success(T data){
        return new Response<>(200, data);
    }

    public Response<T> fail(int errorCode, T data){
        return new Response<>(errorCode,data);
    }

    public Response<T> set(int code, T data){
        return new Response<>(code, data);
    }

}
