package com.example.config;

import com.example.entity.RestBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

/**
 * SecurityConfiguration 是 Spring Security 的配置类，
 * 负责定义应用程序的安全策略，包括认证、授权和登出逻辑，
 * keypoint： 该类使用了方法链式调用（Fluent API）模式，使配置逻辑更加简洁直观。
 * keypoint： 假设我有一个对象a，然后我们调用a的方法，每次调用之后我们还是直接返回a对象，然后我们就可以继续对返回的这个a对象继续调用方法
 * <pre>
 *     {@code
 *     public HttpSecurity authorizeHttpRequests(Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer) {
 *     customizer.customize(this.getOrApply(new AuthorizeHttpRequestsConfigurer(this.getContext())).getRegistry());
 *     return this;  // 返回 HttpSecurity 自身
 * }
 *     }
 * </pre>
 */

@Configuration
public class SecurityConfiguration {

    /**
     * 配置安全过滤器链，定义应用程序的安全规则和处理逻辑。
     * 使用 HttpSecurity 进行链式调用，逐步配置授权、登录和登出逻辑。
     *
     * @param http HttpSecurity 对象，允许配置 Web 安全细节。
     * @return SecurityFilterChain 安全过滤器链，定义整个安全过滤逻辑。
     * @throws Exception 配置过程中可能抛出的异常。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 方法链式调用，通过返回 HttpSecurity 自身，逐步配置安全规则
        return http
                // 授权请求配置，允许针对不同路径定义不同的访问权限
                /* keypoint authorizeHttpRequests方法接受一个 实现了Customizer<T>接口的 类 作为其参数，
                            而所有满足Customizer<T>接口的类，都 有且仅有一个 返回值为空，参数只有1个，且类型为T 的方法，
                            而lambda表达式在实现函数式接口的时候，实际上是实现了这个接口中唯一的方法（a->b中的a就是这个方法的参数，b就是这个方法的实现），
                            所以在这个例子里的conf->...就是Customizer<T>接口中 那个唯一方法的参数，类型为T，
                            实际上，authorizeHttpRequests方法接受参数的T是AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry，
                            所以conf就是一个AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry类型的参数

                 */
                .authorizeHttpRequests(conf -> conf
                        // 允许访问 /api/auth/** 下的所有请求，无需认证
                        .requestMatchers("/api/auth/**").permitAll()
                        // 其他所有请求都需要进行身份验证
                        .anyRequest().authenticated()
                )
                // 配置表单登录
                .formLogin(conf -> conf
                        // 指定处理登录请求的 URL
                        .loginProcessingUrl("/api/auth.login")
                        // 登录失败时，调用自定义失败处理器
                        .failureHandler(this::onAuthenticationFailure)
                        // 登录成功时，调用自定义成功处理器
                        .successHandler(this::onAuthenticationSuccess)
                )
                // 配置登出逻辑
                .logout(conf -> conf
                        // 指定处理登出请求的 URL
                        .logoutUrl("/api/auth/logout")
                        // 登出成功时，调用自定义登出处理器
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 禁用 CSRF 保护（开发阶段或API服务常禁用）
                .csrf(AbstractHttpConfigurer::disable)
                // 使用 build() 构建 SecurityFilterChain 实例
                .build();
    }

    /**
     * 自定义登录失败处理逻辑。
     * 该方法在登录失败时调用，返回 "fail" 字符串提示用户。
     * 注意：该方法返回 "success" 字符串，可能存在笔误，实际应返回 "fail"。
     *
     * @param request  HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param exception 登录失败时抛出的异常，包含失败原因。
     * @throws IOException 写响应时可能抛出的异常。
     * @throws ServletException Servlet 相关异常。
     */
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.fail(401,exception.getMessage()).asJsonString());
    }

    /**
     * 自定义登录成功处理逻辑。
     * 登录成功时返回 "success" 字符串提示用户。
     * 注意：该方法返回 "fail" 字符串，可能存在笔误，实际应返回 "success"。
     *
     * @param request  HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param authentication 认证对象，包含登录用户的相关信息。
     * @throws IOException 写响应时可能抛出的异常。
     * @throws ServletException Servlet 相关异常。
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.success().asJsonString());
    }

    /**
     * 自定义登出成功处理逻辑。
     * 用户登出后可执行的逻辑（如清理会话或返回特定响应）。
     *
     * @param request  HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param authentication 当前用户认证信息，在登出时可能为 null。
     * @throws IOException 写响应时可能抛出的异常。
     * @throws ServletException Servlet 相关异常。
     */
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        // 登出成功后未指定逻辑，保持空实现
    }
}
