package com.example.utils;

/**
 * 定义整个项目会用到的一些常亮
 */
public class Const {
    //存储在redis中表明jwt黑名单的前缀
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";

    //我们需要跨域请求，因为这是一个前后端分离的项目，所以我们需要告诉后端哪些非本项目请求是可以被接受的，所以我们需要一个过滤器，
    // 而这个过滤器应该在security自带的过滤器之前，security自带过滤器order为-100，所以我们这个过滤器应该比这个值更小
    public static final int ORDER_CORS = -102;
}
