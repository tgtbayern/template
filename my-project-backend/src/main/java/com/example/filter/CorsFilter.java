package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 标记该类为Spring组件，使其可以被自动扫描和注册
@Component
// 指定过滤器的执行顺序，ORDER_CORS常量（在const类中自定义的）控制执行的优先级
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {  // 继承HttpFilter，表示该类是一个HTTP过滤器

    // 重写HttpFilter的doFilter方法，处理每个HTTP请求
    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        // 调用添加CORS头的方法，允许跨域访问
        this.addCorsHeader(request, response);

        // 将请求继续传递给下一个过滤器或目标资源
        chain.doFilter(request, response);
    }

    // 添加CORS头信息，允许跨域请求
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        // 允许的跨域来源，动态获取请求的Origin头
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));

        // 允许的HTTP方法，支持多种请求方式
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 允许的请求头，指定客户端可以携带Authorization和Content-Type头
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
}
