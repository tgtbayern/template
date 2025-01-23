package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.response.ConfirmResetVO;
import com.example.entity.vo.response.EmailResetVO;
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

    /**
     * 当通过邮箱注册的时候，调用这个方法，会最终在数据库中增加一个账户
     * @param vo 前端给的信息被封装在这个类里（email，验证码，用户名，密码）
     * @return 成功还是失败的信息
     */
    String registerEmailAccount(EmailRegisterVO vo);

    String resetConfirm(ConfirmResetVO vo);

    String resetEmailAccountPassword(EmailResetVO vo);
}
