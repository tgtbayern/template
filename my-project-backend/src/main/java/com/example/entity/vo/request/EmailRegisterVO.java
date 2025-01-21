package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 当用邮箱注册的时候,前端需要向后端提交相关信息,后端可以用这个类来包装并接收这些信息
 */
@Data
public class EmailRegisterVO {

    @Email  // 确保邮箱字段是合法的电子邮件地址格式
    String email;  // 用户邮箱

    @Length(max = 6, min = 6)  // 验证字符串长度必须正好是 6
    String code;  // 验证码

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")  // 验证用户名只能包含字母、数字和中文字符
    @Length(min = 1, max = 10)  // 验证字符串长度在 1 到 10 之间
    String username;  // 用户名

    @Length(min = 6, max = 20)  // 验证字符串长度在 6 到 20 之间
    String password;  // 用户密码

}
