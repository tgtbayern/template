package com.example.entity.vo.response;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

// 当我们需要重置密码的时候,用户会先输入邮箱,然后我们发送验证码,然后用户提交邮箱,我们验证邮箱是否存在
// 如果存在,就发送验证码到指定邮箱,然后验证邮箱和验证码是否合法
// 如果合法就继续执行,开始真正修改密码,用户输入新密码,然后前端向后端发送用户之前输入的邮箱,验证码,和刚刚输入的密码
// 先通过邮箱找到密码,然后set这个账户密码为用户输入的新值
@Data
public class EmailResetVO {
    @Email
    String email;

    @Length(min = 6, max = 6)
    String code;

    @Length(min = 5, max = 20)
    String password;
}
