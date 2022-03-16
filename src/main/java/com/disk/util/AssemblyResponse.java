package com.disk.util;

public class AssemblyResponse<T> {

    private Response<T> response;

    public Response<T> success(T data){
        return new Response<>(200, data,true);
    }

    public Response<T> fail(int errorCode, T data){
        return new Response<>(errorCode,data,false);
    }

}
