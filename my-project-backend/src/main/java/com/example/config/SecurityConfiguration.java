package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

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
    @Resource
    JwtUtils utils;

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService service;
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
        System.out.println("Filter chain is being configured...");

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
                        .requestMatchers("/error").permitAll()
                        // 其他所有请求都需要进行身份验证
                        .anyRequest().authenticated()
                )
                // 配置表单登录
                .formLogin(conf -> conf
                        // 指定处理登录请求的 URL
                        .loginProcessingUrl("/api/auth/login")
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
                // 配置异常的处理方法
                .exceptionHandling(conf -> conf
                        //未授权异常：访问的资源需要授权（其实是登录）但是没有授权
                        .authenticationEntryPoint(this::onUnauthorized)
                        //无权限异常：普通用户访问管理员权限的网页
                        .accessDeniedHandler(this::onAccessDeny))
                // 禁用 CSRF 保护（开发阶段或API服务常禁用）
                .csrf(AbstractHttpConfigurer::disable)
                // 表示应用程序不会创建或使用 HTTP 会话，每次请求都必须携带认证信息（如 JWT）
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // UsernamePasswordAuthenticationFilter专门处理基于表单登录的用户名和密码认证请求
                // 当用户提交用户名和密码登录时，UsernamePasswordAuthenticationFilter 会拦截请求并执行自定义的认证逻辑
                // 这个方法只是改变了jwtAuthorizeFilter这个过滤器在过滤器链中的顺序，
                // 即使不写这个方法，jwtAuthorizeFilter也会在每次请求的时候执行，除非我们不将jwtAuthorizeFilter注册为bean
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                // 使用 build() 构建 SecurityFilterChain 实例
                .build();
    }

    public void onAccessDeny(HttpServletRequest request,
                               HttpServletResponse response,
                             AccessDeniedException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString());
    }


    /**
     * 自定义未授权访问的处理逻辑。当用户尝试访问需要身份认证的资源但未通过认证时触发。（不是登录失败）
     * - 请求中未携带有效的身份验证凭据（如 JWT）。
     * - 认证信息不完整或无效。
     *
     * 该方法会将未授权的错误信息以 JSON 格式返回给客户端
     */
    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
        System.out.println("Request URL: " + request.getRequestURI()+" in onUnauthorized");

    }


    /**
     * 自定义‘登录’失败处理逻辑。当用户尝试登录（提交认证信息）但认证失败时触发。比如：用户名或密码错误。登录请求中缺少必需的字段。登录认证过程中出现异常。
     * 该方法在登录失败时调用，返回 "fail" 字符串提示用户。
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
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
        System.out.println("Request URL: " + request.getRequestURI()+" in onAuthenticationFailure");
    }

    /**
     * 自定义登录成功处理逻辑。
     * 登录成功时返回 "success" 字符串提示用户。
     *
     * @param request  HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param authentication 认证对象，包含登录用户的相关信息。
     * Authentication 存储的核心信息：
     * 字段	        说明	                                     常见类型	                示例值
     * principal	认证用户的主体，表示用户身份	                 UserDetails或用户名字符串	User 对象
     * credentials	用户凭证，通常是密码	                     String	                    123456
     * authorities	用户权限列表，表示用户拥有的角色或权限	     List<GrantedAuthority>	    [ROLE_ADMIN, ROLE_USER]
     * details	    额外的请求详细信息，如IP地址、设备等	         WebAuthenticationDetails等	IP: 192.168.1.1
     * authenticated	表示用户是否认证成功	                 boolean	                true/false
     *
     * @throws IOException 写响应时可能抛出的异常。
     * @throws ServletException Servlet 相关异常。
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        /*
        keypoint User 是 Spring Security 提供的一个 UserDetails 的默认实现类，通常用来封装用户的身份和权限信息。
                 authentication下principal字段包含的属性可以让其转化为一个user对象
                 而UserDetails是一个接口，定义了用户的核心属性，例如用户名、密码、账户是否锁定、权限等，其属性比user更多
         */
        User user = (User) authentication.getPrincipal();

        //这里的username是loadUserByUsername方法存储的username
        Account account = service.findAccountByNameOrEmail(user.getUsername());

        /*keypoint
           通过调用我们自己写的方法，生成登录成功后的token
           然后将生成的token 和 其他信息一起封装在vo中，返回给前端
         */
        String token = utils.createJwt(user, account.getId(), account.getUsername());
        AuthorizeVO vo=new AuthorizeVO();

        vo.setRole(account.getRole());
        vo.setExpire(utils.expireTime());
        vo.setToken(token);
        vo.setUsername(account.getUsername());
        response.setContentType("application/json;charset=utf-8");

        /*keypoint  restBean是我们自己写的一个实体类，当登录成功后，会把需要返回的消息封装到这个类中，返回给前端
                    当我们成功登录后，我们将生成的vo放入restBean类的success方法
                    success方法会将这个参数————vo，向restBean实体类的data字段注入
         */
        response.getWriter().write(RestBean.success(vo).asJsonString());
        System.out.println("Request URL: " + request.getRequestURI()+" in onAuthenticationSuccess");
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
    // 处理用户登出成功的逻辑
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        // 设置响应的内容类型为 JSON，编码格式为 UTF-8
        response.setContentType("application/json;charset=utf-8");

        // 获取响应的输出流，用于返回 JSON 数据给前端
        PrintWriter writer = response.getWriter();

        // 从请求头中获取名为 "Authorization" 的 JWT 令牌
        String authorization = request.getHeader("Authorization");

        // 调用工具类方法，尝试使该 JWT 令牌失效
        if (utils.invalidateJwt(authorization)) {
            // 如果令牌失效成功，返回操作成功的 JSON 响应
            writer.write(RestBean.success().asJsonString());
        } else {
            // 如果令牌失效失败，返回失败信息，状态码 400，提示登出失败
            writer.write(RestBean.fail(400, "退出登录失败").asJsonString());
        }
    }

}
