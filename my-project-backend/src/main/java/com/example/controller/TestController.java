package com.example.controller;

import com.example.entity.RestBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController 用于测试 Spring Security 配置是否生效，
 * 提供一个简单的受保护和开放接口示例。
 *
 @RestController 是 Spring Framework 提供的一个注解，用于简化创建 RESTful Web 服务。

 标识类是一个控制器，用于处理 HTTP 请求。
 返回 JSON 或 XML 格式的数据（默认返回 JSON）。
 相当于 @Controller 和 @ResponseBody 的组合，表示方法返回的结果直接作为 HTTP 响应体返回，而不是解析为视图。
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
