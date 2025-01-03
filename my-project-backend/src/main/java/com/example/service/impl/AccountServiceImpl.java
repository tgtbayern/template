package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 标识该类是一个 Service 组件（业务层 Bean），表示它承担服务功能，通常编写具体的业务逻辑。
//自动注册到 Spring 容器中，成为 Spring 管理的 Bean，供其他组件（如 Controller）自动装配和调用。
//主要用于 Service 层，但本质上与 @Component 功能相同，只是语义化更清晰，表示业务逻辑。
@Service
public class AccountServiceImpl
        // 继承 ServiceImpl，使用 MyBatis-Plus 提供的基础增删改查功能
        // ServiceImpl<M extends BaseMapper<T>, T>    M —— 你的 Mapper 接口，通常继承自 BaseMapper    T —— 你的 实体类，对应数据库中的表。
        extends ServiceImpl<AccountMapper, Account>
        // 实现 AccountService 接口，提供自定义业务逻辑
        implements AccountService {

    /**
     * 根据用户名或邮箱加载用户信息（用于 Spring Security 登录认证）
     * @param username 登录时输入的用户名或邮箱
     * @return UserDetails 用户详细信息（用户名、密码、角色等）
     * @throws UsernameNotFoundException 如果找不到用户，抛出此异常
     *
     * 这个类实现了自定义的AccountService接口，而这个接口又继承自UserDetailsService，
     * UserDetailsService中有一个loadUserByUsername方法，所以这里的@Override重写的是UserDetailsService的方法
     * 一旦security检测到这个方法被重写，就会自动调用这个方法的逻辑来判断用户是否合法
     * 如果这个方法没有被重写，security就会自动生成一个username=user，pwd=控制台的对象让我们登录
     *
     * 所以这里的username实际上不一定是真正的username，而是登录时使用的主键，如果用邮箱登录，这里的username就是邮箱，如果用用户名登录，这里的username就是用户名
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过用户名或邮箱查询账号
        Account account = this.findAccountByNameOrEmail(username);
        // 如果账号不存在，抛出用户名或密码错误的异常
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");

        // 使用 Spring Security 提供的 User 类构建用户信息
        return User
                .withUsername(username)  // keypoint 设置用户名，如果用邮箱登录，那么之后所有userDetail读出的username都是邮箱
                .password(account.getPassword())  // 设置密码
                .roles(account.getRole())  // 设置用户角色
                .build();  // 构建 UserDetails 对象
    }

    /**
     * 根据用户名或邮箱查询用户信息
     * @param text 在上面的loadUserByUsername方法调用时，传入的参数可能是用户名或邮箱
     * @return Account 返回查询到的账号对象
     */
    public Account findAccountByNameOrEmail(String text) {
        // 使用 MyBatis-Plus 提供的 query() 方法构建查询条件
        return this.query()
                // 查询条件：匹配用户名
                .eq("username", text).or()
                // 或者匹配邮箱
                .eq("email", text)
                // 返回匹配的第一个记录
                .one();
    }
}

