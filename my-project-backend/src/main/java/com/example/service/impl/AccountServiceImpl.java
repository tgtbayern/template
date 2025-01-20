package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    // 这个类的介绍见最下面
    @Resource
    AmqpTemplate amqpTemplate;

    // 这个类的介绍见最下面
    @Resource
    StringRedisTemplate stringRedisTemplate;

    // 自定义的限流工具
    @Resource
    FlowUtils flowUtils;

    /**
     * 根据用户名或邮箱加载用户信息（用于 Spring Security 登录认证）
     * @param username 登录时输入的用户名或邮箱
     * @return UserDetails 用户详细信息（用户名、密码、角色等）
     * @throws UsernameNotFoundException 如果找不到用户，抛出此异常
     *
     * 这个类实现了自定义的AccountService接口，而这个接口又继承自UserDetailsService，
     * UserDetailsService中有一个loadUserByUsername方法，所以这里的@Override重写的是UserDetailsService的方法
     * 一旦security检测到这个方法被重写，就会自动调用这个方法的逻辑来判断用户是否合法
     * 如果这个方法没有被重写，security就会自动生成一个username=user，pwd=控制台输出的那个密码 的对象让我们登录
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
     * @param text 在上面的loadUserByUsername方法调用时，传入的参数可能是用户名或邮箱,所以这里的text可能是用户名也可能是邮箱
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

    /**
     *
     * @param type 需要发送的邮件类型(注册邮件？还是更改邮箱的邮件？还是别的类型的邮件)
     * @param email 需要发送到的地址
     * @param ip 发起这个发送邮件请求的ip，为了防止同一个ip短时间内多次请求
     * @return
     */
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        //intern()方法会检查字符串池中是否已经存在当前字符串：
        //如果存在，返回该字符串的引用。
        //如果不存在，将该字符串添加到池中，并返回它的引用。
        //通过ip.intern()，即使不同线程中的ip变量是不同的对象，但只要字符串内容相同，它们都会引用池中的同一个字符串对象。
        // 这里的操作不是为了限流，而是确定在同一时间一个ip只有一个访问（假如一次访问时间很短，即使加锁依然可以在短时间内多次访问，所以这里的锁不是限流，限流是通过redis做到的）
        synchronized (ip.intern()){
            // 这个方法将生成一个唯一由ip确定的key，然后检查redis中是否有同样的key，如果有就返回false，然后就会进入这个if
            if(!this.verifyLimit(ip)){
                return "请求频繁，请稍后再试";
            }

            Random random = new Random();
            int code = random.nextInt(899999) + 100000;//生成验证码

            // 创建一个Map对象，保存验证码相关的数据，包括类型、邮箱和验证码本身，这一整个map（而不是map中的某个元素）相当于mq中的一个数据
            // map.of() 返回一个包含指定键值对的 Map，这个 Map 是 不可变的
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);

            // 使用RabbitMQ将验证码数据发送到名为"mail"的消息队列中 (消息队列的名称可以在java中指定，也可以在mq的客户端设置)
            amqpTemplate.convertAndSend("emailQueue", data);

            // 将验证码存入Redis中，设置的key格式为：verify:email:data:<邮箱地址>
            // 验证码字符串形式存储，并设置过期时间为3分钟
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);

            // 方法返回null，这里可以根据需求改成返回生成的验证码或其他提示信息
            return null;
        }

    }

    private boolean verifyLimit(String ip) {
        // 生成一个限流的key，通常由常量前缀(自己定义在const类中)和IP地址拼接而成，确保唯一性
        String key = Const.VERIFY_EMAIL_LIMIT + ip;

        // 调用FlowUtils中的limitOnceCheck方法，尝试对该key设置限流标记，限流时间为60秒
        return flowUtils.limitOnceCheck(key, 60);
    }


}

/**
 AmqpTemplate 是 Spring AMQP（Advanced Message Queuing Protocol）中用于与 RabbitMQ 等消息代理交互的接口。
 它提供了同步发送和接收消息的便捷方法，封装了底层 AMQP 细节，使开发者可以通过面向对象的方式操作消息队列。
 主要作用:1 发送消息到交换机或队列                  2 接收消息从队列中消费
 3 支持消息转换（Java对象与AMQP消息互转）   4 简化消息通信流程，提供模板方法模式的实现

 常见用法示例
 1. 发送消息到队列
    @Autowired
    private AmqpTemplate amqpTemplate;
    public void sendMessage(String message) {
        amqpTemplate.convertAndSend("emailQueue", message);
    }
 convertAndSend：将消息转换为 AMQP 消息，并发送到指定队列或交换机. emailQueue 是队列的名字。
 2. 发送消息到交换机并指定路由键
    amqpTemplate.convertAndSend("exchangeName", "routingKey", message);
    消息会通过exchangeName交换机，根据routingKey路由到目标队列。
 3. 接收消息
    String message = (String) amqpTemplate.receiveAndConvert("emailQueue");
    receiveAndConvert：从指定队列接收消息，并自动将消息转换为指定类型。
 */

//-----------------------------------------------------------------------------------------------------------------

/**
 * `StringRedisTemplate` 是 Spring Data Redis 提供的一个工具类，专门用于操作 Redis 数据库中的字符串类型数据。
 * 它是 `RedisTemplate` 的子类，简化了字符串类型的键值操作，省去了序列化和反序列化的复杂性。

 *  主要作用
 * - 简化对 Redis 键值对的存取操作,自动处理字符串的序列化与反序列化
 * - 适用于操作字符串类型的键值，无需自定义序列化器
 * - 提供了便捷的方法，操作方式类似 `RedisTemplate`，但直接面向字符串
 * ---------------------------------------------------------------------------
 *  常见用法示例
 *  1. 存储数据到 Redis
 * @Autowired
 * private StringRedisTemplate stringRedisTemplate;
 *
 * public void saveToRedis(String key, String value) {
 *     stringRedisTemplate.opsForValue().set(key, value);
 * }
 * ```
 * - `opsForValue()`：表示操作 Redis 中的**字符串类型**数据（类似于 `SET` 命令）。
 * ---------------------------------------------------------------------------
 *  2. **从 Redis 读取数据**
 * public String getFromRedis(String key) {
 *     return stringRedisTemplate.opsForValue().get(key);
 * }
 * ---------------------------------------------------------------------------
 *3. **设置带过期时间的键值对**
 * stringRedisTemplate.opsForValue().set("token", "abc123", 10, TimeUnit.MINUTES);
 * - 该键将在**10分钟后过期**。
 * ---------------------------------------------------------------------------
 *  4. **原子递增或递减**
 * stringRedisTemplate.opsForValue().increment("counter", 1);
 * stringRedisTemplate.opsForValue().decrement("counter", 1);
 * ```
 * - Redis 的原子性递增（`INCR`）和递减（`DECR`）操作。
 * ---------------------------------------------------------------------------
 * 5. **删除键**
 * stringRedisTemplate.delete("token");
 * ---------------------------------------------------------------------------
 *  使用场景
 * - **存储和读取缓存数据**，如验证码、Token 等
 * - **实现分布式锁**，利用 `SETNX` 命令
 * - **计数器实现**，通过 `increment` 和 `decrement` 原子操作
 * - **限流与防重处理**
 * ---------------------------------------------------------------------------
 *  和 `RedisTemplate` 区别
 * - **`RedisTemplate`**：可以操作复杂对象，需要手动配置序列化器（如 Jackson）。
 * - **`StringRedisTemplate`**：直接操作字符串类型数据，序列化器默认使用 `StringRedisSerializer`，更简单。
 */

