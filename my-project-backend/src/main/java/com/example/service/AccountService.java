package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

//IService 是 MyBatis-Plus 提供的一个通用服务接口，它定义了一套常用的 CRUD（增删改查）操作，简化了对数据库的访问
public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    /**
     *
     * @param type 需要发送的邮件类型
     * @param email 需要发送到的地址
     * @param ip 发起这个发送邮件请求的ip，为了防止同一个ip短时间内多次请求
     * @return
     */
    String registerEmailVerifyCode(String type, String email, String ip);
}
