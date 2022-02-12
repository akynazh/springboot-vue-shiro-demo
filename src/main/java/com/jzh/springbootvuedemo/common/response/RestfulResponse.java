package com.jzh.springbootvuedemo.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0
 * @description 前后端交互数据
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:02
 */
@Data
public class RestfulResponse implements Serializable {
    private int code;
    private String message;
    private Object data;

    public static RestfulResponse response(int code, String message, Object data) {
        RestfulResponse response = new RestfulResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static RestfulResponse success(Object data) {
        return response(200, "操作成功", data);
    }

    public static RestfulResponse success() {
        return response(200, "操作成功", null);
    }

    public static RestfulResponse fail(int code, String message) {
        return response(code, message, null);
    }
}
