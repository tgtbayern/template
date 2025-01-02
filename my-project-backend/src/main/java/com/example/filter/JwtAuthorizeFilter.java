package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * security本身进行验证的时候，是不会按照jwt进行验证的，
 * 1 所以既然我们选用了jwt验证，我们就需要将每次的验证请求进行拦截，从中拦截出jwt，在过滤器链中加上对jwt的验证
 * 2 直接调用resolveJwt方法，验证jwt是否合法
 * 3 如果合法，从jwt中得到这个user的相关信息，封装在UserDetails里
 * 4 将UserDetails封装为UsernamePasswordAuthenticationToken，此时状态是未认证（authenticated = false）
 *   这个UsernamePasswordAuthenticationToken类中的数据不会被用于验证，只是为了表明此时这个用户还没有被验证（实际上因为jwt已经验证合法了，所以这个用户已经被验证过了）
 * 5 从WebAuthenticationDetailsSource中获取一些网络信息，比如ip地址等 不储存在jwt中的数据（当然也就不在UserDetails里，因为UserDetails中的数据是从jwt来的），这些数据在后续的验证中也有用。
 * 6 将UsernamePasswordAuthenticationToken放到SecurityContextHolder中，这个动作就表明这个token（或者说这个用户）验证通过
 * 7 回到总的过滤器链，进行后续的验证
 */
@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取 Authorization 字段，通常包含 JWT 令牌
        String authorization = request.getHeader("Authorization");
        // 解析 JWT 令牌，获取解码后的 DecodedJWT 对象，这一步会判断jwt是否合法
        DecodedJWT jwt = utils.resolveJwt(authorization);
        // 如果 JWT 令牌有效，进行用户认证逻辑
        if (jwt != null) {
            // 通过 JWT 解析用户信息，生成 UserDetails 对象
            UserDetails user = utils.toUser(jwt);

            // 创建 Spring Security 的认证对象，包含用户信息和权限
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            // 设置请求的详细信息，比如 IP 地址、session 等
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 将认证对象设置到 SecurityContextHolder，完成安全上下文认证
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将用户 ID 作为请求属性存入，供后续业务逻辑使用
            // 如果后续的业务逻辑多次需要从 JWT 中解析用户的某些信息（如用户 ID），每次重新解析 JWT 会带来额外的计算成本。
            //将解析后的 id 提前存入 request 的属性中，避免多次解析，直接复用。
            request.setAttribute("id", utils.toId(jwt));
        }

        // 继续过滤器链，放行请求
        filterChain.doFilter(request, response);
    }
}

