package com.example.utils;

/**
 * 定义整个项目会用到的一些常亮
 */
public class Const {
    //存储在redis中的数据的说明头，表明jwt黑名单的前缀
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";

    //TODO 存储在redis中的数据的说明头，表明
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    //存储在redis中的数据的说明头，表明这个数据是email的相关数据
    public static final String VERIFY_EMAIL_DATA = "verify:email:data:";

    //我们需要跨域请求，因为这是一个前后端分离的项目，所以我们需要告诉后端哪些非本项目请求是可以被接受的，所以我们需要一个过滤器，
    // 而这个过滤器应该在security自带的过滤器之前，security自带过滤器order为-100，所以我们这个过滤器应该比这个值更小
    public static final int ORDER_CORS = -102;

    // 用于限流的filter的优先级
    public static final int ORDER_LIMIT = -101;

    // 指定ip的计数器,后面表明这个ip在规定时间内已经请求的次数
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";

    // 封禁的头,后接ip,表明这个ip已经被封禁
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";



}
