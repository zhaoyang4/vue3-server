package com.example.userserver.common;

import lombok.Data;

/**
 * 统一接口返回结构。
 *
 * 前端和后端约定一个固定的返回格式，前端处理起来最省心：
 * {
 *   "code": 0,        // 0 表示成功，非 0 表示失败（业务错误）
 *   "message": "ok",  // 提示信息
 *   "data": {...}     // 真正的业务数据（成功时才有）
 * }
 *
 * 用泛型 <T> 让 data 可以是任意类型（User、Page<User>、Boolean...）。
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    /** 成功：带数据 */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMessage("ok");
        r.setData(data);
        return r;
    }

    /** 失败：带错误信息 */
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setCode(1);
        r.setMessage(message);
        return r;
    }
}
