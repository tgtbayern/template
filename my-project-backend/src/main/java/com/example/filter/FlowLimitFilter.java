package com.example.filter;

import com.example.entity.RestBean; // 一个实体类，用于封装响应信息。
import com.example.utils.Const; // 一个常量类，定义了一些固定的常量（如ORDER_LIMIT、FLOW_LIMIT_BLOCK等）。
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order; // 用于定义过滤器的执行顺序。
import org.springframework.data.redis.core.StringRedisTemplate; // Spring 提供的 Redis 操作模板类。
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 这是一个限流过滤器，用于限制客户端的访问频率。
 * 如果某个 IP 的请求频率超过阈值（10 次请求 / 3 秒），
 * 将阻止该 IP 的后续请求 30 秒。
 */
@Component // 将此类声明为 Spring 的组件，方便 Spring 容器管理。
@Order(Const.ORDER_LIMIT) // 定义过滤器在过滤器链中的执行顺序。
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template; // 注入 Redis 操作模板，用于与 Redis 进行交互。

    /**
     * 过滤方法：对每个 HTTP 请求进行处理。
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param chain    过滤器链，用于继续处理请求
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr(); // 获取客户端的 IP 地址。
        if (this.tryCount(address)) { // 判断 IP 是否被限流。
            chain.doFilter(request, response); // 如果未被限流，回到过滤器链,继续处理请求。
        } else {
            this.writeBlockMessage(response); // 如果被限流，返回禁止访问的消息。
        }
    }

    /**
     * 返回禁止访问的响应信息。
     * @param response HTTP 响应对象
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 设置 HTTP 状态码为 403（禁止访问）。
        response.setContentType("application/json"); // 设置响应类型为 JSON。
        response.getWriter().write(RestBean.forbidden("操作频繁, 请稍后再试").asJsonString());
        // 返回 JSON 格式的错误消息。
    }

    /**
     * 尝试增加当前 IP 的访问计数，并判断是否需要限流。
     * @param ip 客户端 IP 地址
     * @return 如果未超过限流阈值，返回 true；否则返回 false。
     */
    private boolean tryCount(String ip) {
        synchronized (ip.intern()) { // 基于 IP 字符串加锁，确保线程安全。
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip))) {
                // 检查 Redis 是否存在 IP 对应的限流键。
                return false; // 如果存在限流键，直接返回 false。
            }
            return this.limitPeriodCheck(ip); // 如果未被限流，检查请求频率。
        }
    }

    /**
     * 检查 IP 是否在指定时间窗口内超过访问频率限制。
     * @param ip 客户端 IP 地址
     * @return 如果请求频率未超限，返回 true；否则返回 false。
     */
    private boolean limitPeriodCheck(String ip) {
        if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_COUNTER + ip))) {
            // 检查 Redis 是否存在该 IP 的计数器键。
            /**
             * Optional.of(...)
             * 将 increment() 的返回值包装成一个 Optional 对象。
             * Optional 是 Java 8 引入的一个工具类，用来处理可能为 null 的值，防止 NullPointerException。
             * .orElse(0L)
             * 如果 Optional 中的值为 null（理论上这里不会发生，但为了安全处理异常情况），则返回默认值 0L。
             * 这样可以确保 increment 变量永远不为 null，即使 Redis 出现异常或返回了 null。
             */
            long increment = Optional.of(template.opsForValue()
                    .increment(Const.FLOW_LIMIT_COUNTER + ip)).orElse(0L);
            // 如果存在计数器键，尝试将计数器值 +1。
            if (increment > 10) {
                // 如果计数器值超过 10 次（限流阈值），将 IP 限流。
                template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip, "", 30, TimeUnit.SECONDS);
                // 设置限流键，有效期为 30 秒。
                return false; // 返回 false，表示已被限流。
            }
        } else {
            // 如果计数器键不存在，初始化计数器，设置有效期为 3 秒。
            template.opsForValue().set(Const.FLOW_LIMIT_COUNTER + ip, "1", 3, TimeUnit.SECONDS);
        }
        return true; // 返回 true，表示未超出请求限制。
    }
}
