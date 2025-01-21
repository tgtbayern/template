package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated //可以对参数进行校验，确保传入的参数符合我们要求的格式
@RestController // 声明这是一个控制器类，所有方法返回的数据会直接作为 HTTP 响应的 JSON 格式
@RequestMapping("/api/auth") // 为当前控制器类定义统一的请求路径前缀 "/api/auth"
public class AuthorizeController {

    @Resource // 自动注入 AccountService 类的实例，提供账户相关的业务逻辑服务
    AccountService service;

    @GetMapping("/ask-code") // 映射 GET 请求到 "/api/auth/ask-code" 接口
    public RestBean<Void> askVerifyCode(
            @RequestParam @Email String email, // 从请求参数中获取 email 参数，必须符合email的格式
            @RequestParam @Pattern(regexp = "(register|reset)") String type,  // 从请求参数中获取 type 参数，只能为register或者reset
            HttpServletRequest request  // 自动注入 HTTP 请求对象
    ) {
        // 调用业务层方法，向指定邮箱发送验证码
        // 参数包含验证码类型、邮箱地址和客户端的 IP 地址
        String message = service.registerEmailVerifyCode(type, email, request.getRemoteAddr());

        // 根据业务逻辑的返回值判断是否成功
        // 如果 message 为 null，表示操作成功，返回成功的响应体
        // 否则，返回失败的响应体，并带有错误码 400 和错误信息
        return message == null ? RestBean.success() : RestBean.fail( 400, message);
    }

    @PostMapping("/register")
    //@Valid会根据 EmailRegisterVO 类中字段上的校验注解（如 @NotNull、@Size、@Email 等）自动验证传入的数据。
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo) {
        // 将前端返回并包装好的数据传给业务代码执行注册的真正程序,返回执行的结果
        String result = service.registerEmailAccount(vo);
        return result == null ? RestBean.success() : RestBean.fail(400, result);
    }
}

