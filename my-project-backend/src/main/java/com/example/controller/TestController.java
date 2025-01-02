package com.example.controller;

import com.example.entity.RestBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController 用于测试 Spring Security 配置是否生效，
 * 提供一个简单的受保护和开放接口示例。
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

//    @GetMapping("/open")
//    public RestBean<String> openEndpoint() {
//        return RestBean.success("这个接口无需认证，可以直接访问！");
//    }

    @GetMapping("/hello")
    public String test() {
        return "hello world";
    }
}
