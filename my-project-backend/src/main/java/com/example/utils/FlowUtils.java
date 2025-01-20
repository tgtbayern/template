package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component  // 将FlowUtils注册为Spring组件，供容器管理
public class FlowUtils {

    @Resource  // 注入Spring的StringRedisTemplate，用于操作Redis
    StringRedisTemplate template;

    /**
     * 为接口限流，比如：当一个ip请求向某个邮箱发送一封邮件的时候，我们就会调用这个方法
     * 我们会生成一个key，这个key对于同一个ip地址来说是永远一样的
     * 然后调用这个方法，首先检查redis中是否有这个key，如果有，就说明这个ip地址还处于冷却时间中，不对这个地址的这条请求做出响应
     * 如果没有key，就说明这个ip的这个请求可以被响应，同时把这个key假如redis中，表明这个ip的冷却时间开始
     *
      */

    public boolean limitOnceCheck(String key, int blockTime) {
        // 判断Redis中是否已存在该key，如果存在则返回false，表示已限流
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            return false;
        } else {
            // 如果key不存在，则设置一个新的key，有效时间为blockTime秒
            template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
            return true;  // 返回true，表示限流设置成功
        }
    }
}
