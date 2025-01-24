package com.example.entity;

// 导入 FastJSON 2.x 库中的 JSONObject 和 JSONWriter，用于 JSON 序列化
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.example.entity.vo.response.AuthorizeVO;

// 定义一个记录类型（record），用于封装 REST API 返回的标准响应格式
// 泛型 T 代表返回的数据类型，可以是任何类型
public record RestBean<T>(int code, T data, String message) {

    // 定义一个静态方法 success，用于构造一个表示成功的 RestBean 响应
    // success 方法接受一个参数 data，表示成功响应中的数据
    public static <T> RestBean<T> success(T data){
        // 返回一个 RestBean 实例，状态码为 200，表示成功，消息为 "success"
        //System.out.println("RestBean: success! "+data);
        return new RestBean<>(200, data, "success");
    }

    // 定义一个重载的静态方法 success，无需传递数据（即成功但没有返回数据）
    public static <T> RestBean<T> success(){
        // 调用上面的 success 方法，传入 null 作为数据
        return success(null);
    }

    // 定义一个静态方法 fail，用于构造一个表示各种可能的失败的 RestBean 响应（无权限，未登录，登录失败等）
    // fail 方法接受两个参数，code（失败的状态码）和 message（失败的消息）
    public static <T> RestBean<T> fail(int code, String message){
        // 返回一个 RestBean 实例，状态码为传入的 code，数据为 null，消息为传入的 message
        return new RestBean<>(code, null, message);
    }

    // 未认证的时候都是401错误
    public static <T> RestBean<T> unauthorized(String message){
        //System.out.println("RestBean: unauthorized! ");
        return fail(401,message);
    }

    // 登录了但是无权限（比如普通用户访问管理员页面）的时候都是403错误
    public static <T> RestBean<T> forbidden(String message){
        //System.out.println("RestBean: forbidden! ");
        return fail(403,message);
    }

    // 定义一个实例方法 asJsonString，将当前 RestBean 对象转换为 JSON 字符串
    public String asJsonString(){
        // 使用 FastJSON 的 JSONObject 工具类将当前对象转换为 JSON 字符串
        // 使用 JSONWriter.Feature.WriteNulls 特性，表示在 JSON 输出时保留 null 值字段
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
