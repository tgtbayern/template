package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    /**
     * 生成 JWT（JSON Web Token）的方法。
     *
     * @param details  包含用户权限信息的 UserDetails 对象
     * @param id       用户的唯一标识 ID
     * @param username 用户名
     * @return         生成的 JWT 字符串
     */
    public String createJwt(UserDetails details, int id, String username) {
        // 使用 HMAC256 算法，并指定密钥 key
        Algorithm algorithm = Algorithm.HMAC256(key);

        // 调用 expireTime() 方法，获取 JWT 的过期时间
        Date expire = this.expireTime();

        // 创建并返回一个 JWT 字符串
        return JWT.create()
                // 将用户 ID 作为自定义声明存入 JWT
                .withClaim("id", id)
                // 将用户名作为自定义声明存入 JWT
                .withClaim("name", username)

                /**
                 * 将用户的权限列表存入 JWT，使用流处理将 GrantedAuthority 转换为字符串列表
                details 是一个实现了 UserDetails 接口的对象,是传入的参数，通常包含用户的基本信息和权限。
                getAuthorities() 方法返回一个 Collection<? extends GrantedAuthority> 类型的集合，表示该用户拥有的权限。
                GrantedAuthority 是一个接口，通常会有一个具体的实现类，比如 SimpleGrantedAuthority。它表示权限或角色。
                 */
                .withClaim("authorities", details.getAuthorities()
                        /**
                        stream() 方法将集合 details.getAuthorities() 转化为 Java 8 的流对象（Stream<GrantedAuthority>）。
                        流（Stream）是一种用于处理集合数据的高级抽象，提供了方便的操作方法
                         */
                        .stream()
                        /**
                        map() 是流的一个操作，用来将流中的元素映射为另一种类型。在这个例子中，它将每个 GrantedAuthority 对象映射为该权限的名称字符串（String）。
                        GrantedAuthority::getAuthority 是一个方法引用，它相当于 x -> x.getAuthority()，
                        即调用 GrantedAuthority 对象的 getAuthority() 方法来获取权限的字符串表示。
                         */
                        .map(GrantedAuthority::getAuthority)
                        .toList())

                // 设置 JWT 的过期时间
                .withExpiresAt(expire)
                // 设置 JWT 的签发时间为当前时间
                .withIssuedAt(new Date())
                // 在生成 JWT（JSON Web Token）时，为令牌设置一个唯一标识（JWT ID, 简称 JTI）
                // 在 Java 中，UUID 由 java.util.UUID 类生成，用于创建唯一标识符。UUID 是通用唯一识别码，长度为 128 位，可以生成随机或基于特定算法的 UUID。
                .withJWTId(UUID.randomUUID().toString())
                // 使用指定算法对 JWT 进行签名
                .sign(algorithm);
    }

    /**
     * 通过对java内置的日期相关类进行操作，设定一个过期时间
     * @return 过期的时间
     */
    public Date expireTime(){
         //获取一个 Calendar 实例，默认情况下它会被设置为当前时间。
         //Calendar 是一个抽象类，用来表示日历的操作，常用于日期计算。
         Calendar calendar = Calendar.getInstance();

         /*
            calendar.add() 是 Calendar 类的方法，用来对当前时间进行加减操作。
            第一个参数是操作的字段，这里是 Calendar.HOUR，表示按小时来调整。
            第二个参数是要加上的值，这里是 expire * 24，意思是将当前时间加上 expire 天的小时数。
            expire 是一个变量，假设它表示天数，那么 expire * 24 就表示将当前时间加上 expire 天后的时间（以小时为单位）。
          */
         calendar.add(Calendar.HOUR,expire*24);

         //通过 getTime() 方法将 Calendar 对象转化为 Date 对象，返回当前加了小时数的时间。
         return calendar.getTime();
     }

    /**
     * 解析并验证 JWT（JSON Web Token）。
     * 该方法接收一个包含 JWT 的 Authorization 头字符串，提取并验证其中的 token，
     * 返回解码后的 JWT 信息（DecodedJWT）。如果 token 无效、过期或解析失败，则返回 null。
     * @param headerToken 包含 Bearer 前缀和 JWT 的完整 Authorization 头字符串。     示例："Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * @return DecodedJWT 对象，如果 token 有效且未过期；否则返回 null。
     */
    public DecodedJWT resolveJwt(String headerToken) {
        // 1. 通过自定义的 convertToken 方法提取出实际的 JWT。如果提取失败，返回 null。
        String token = this.convertToken(headerToken);
        if (token == null)
            return null;

        // 2. 使用配置文件中定义的密钥（key）创建 HMAC256 加密算法实例。key 由 @Value 注解从配置文件中加载。
        Algorithm algorithm = Algorithm.HMAC256(key);

        // keypoint 3. 创建一个 JWTVerifier 实例，将token传入JWTVerifier，JWTVerifier可以判断这个token是否有效。
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            //keypoint 4. 验证 token 是否有效。
            //          如果 token 被篡改或签名不匹配，将抛出 JWTVerificationException。
            //          如果 JWT 签名有效且未过期，验证通过，返回一个 DecodedJWT 对象。
            DecodedJWT verify = jwtVerifier.verify(token);

            // 如果这个jwt过期了
            if(this.isInvalidToken(verify.getId()))
                return null;

            // 5. 没有执行catch语句，说明token验证通过了，提取 token 的过期时间。
            Date expiresAt = verify.getExpiresAt();

            // 6. 检查当前时间是否已经超过 token 的过期时间。如果过期，返回 null。
            //    否则，返回解析后的 DecodedJWT 对象。
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            // 7. token 被篡改或签名不匹配，将抛出 JWTVerificationException，返回 null。
            return null;
        }
    }


    /**
     * 前端向后端发送一个登录的请求的时候，会携带user的jwt信息，而jwt信息前会有一个Bearer前缀
     * 后端为了判断是否可以给user放行，需要解析jwt信息，在解析前需要对这个请求体进行处理，得到正确的jwt串
     * @param headerToken 未经处理的jwt串
     * @return 经过处理的jwt串
     */
     private String convertToken(String headerToken){
        if(headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.replace("Bearer ","");
     }

    /**
     * 将 JWT 令牌转换为 UserDetails 对象的工具方法。
     *
     * 该方法解析传入的 DecodedJWT 对象，提取用户相关信息，
     * 并构建一个 Spring Security 的 UserDetails 实例。
     *
     * @param jwt 已解码的 JWT 令牌对象，包含用户信息的声明（claims）。
     * @return UserDetails 实例，包含用户名、密码（默认为 ***）和用户权限。
     */
    public UserDetails toUser(DecodedJWT jwt) {
        // 从 JWT 中提取所有的 claims（声明），返回为 Map 形式
        Map<String, Claim> claims = jwt.getClaims();

        // 使用 claims 构建 Spring Security 的 UserDetails 对象
        return User
                .withUsername(claims.get("name").asString())  // 从 claims 中获取 "name" 字段作为用户名
                .password("***")  // 密码不从 JWT 中获取，设置为默认的占位符
                .authorities(claims.get("authorities").asArray(String.class))  // 从 claims 中提取用户权限
                .build();  // 构建并返回 UserDetails 对象
    }

    /**
     * 从 DecodedJWT 对象中提取用户的 ID。这个id和我们为了使得jwt失效所设置的jwt的id是不一样的，那个是jwt的id，这个id是用户的id，
     *
     * @param jwt 解码后的 JWT（JSON Web Token）对象，包含了用户的相关信息。
     * @return 用户的 ID，类型为 Integer。如果 "id" 不存在或格式不正确，可能会返回 null。
     */
    public Integer toId(DecodedJWT jwt) {
        // 获取 JWT 中的所有声明 (claims)，返回一个 Map，其中 key 是声明的名称，value 是 Claim 对象。
        Map<String, Claim> claims = jwt.getClaims();

        // 通过声明名 "id" 获取对应的 Claim 对象，并将其转换为 Integer 类型。
        // 如果 "id" 不存在或类型无法转换为整数，则会抛出异常或返回 null。
        return claims.get("id").asInt();
    }

    /**
     * 使指定的 JWT 失效。
     *
     * @param headerToken 请求头中的 JWT 字符串
     * @return 如果 JWT 失效成功返回 true，如果失败或 token 无效则返回 false
     */
    public boolean invalidateJwt(String headerToken) {
        // 将请求头中的 token 转换为实际 JWT 字符串
        String token = this.convertToken(headerToken);
        // 如果转换失败或 token 为空，直接返回 false
        if (token == null) return false;

        // 使用 HMAC256 加密算法和密钥 key 创建验证器
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            // 验证 token 是否有效，如果无效会抛出异常
            DecodedJWT jwt = jwtVerifier.verify(token);
            // 获取 JWT 的唯一标识符 (jti)
            String id = jwt.getId();
            // 删除 token 并返回结果，通过获取jwt过期时间，设定jwt失效库中，每条记录的存在时间
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            // 验证失败，返回 false
            return false;
        }
    }

    /**
     * 将 JWT 添加到黑名单中，使其失效。
     * 如果jwt根本不存在（通过uuid判断），直接返回false
     * 因为jwt本身有过期时间，所以当我们记录某个jwt失效时，一旦超过这个jwt本身的过期时间，就没必要记录了，因为这个jwt已经被删除了
     *
     * @param uuid JWT 的唯一标识符
     * @param time JWT 的过期时间
     * @return 如果 token 已失效或无法添加，返回 false；否则返回 true
     */
    private boolean deleteToken(String uuid, Date time) {
        // 如果 token 已经在黑名单中，直接返回 false
        if (this.isInvalidToken(uuid))
            return false;

        // 当前时间
        Date now = new Date();
        // 计算剩余有效期，如果已过期则设置为 0
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        // 将 token 加入 Redis 黑名单，设置剩余的有效期
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);

        // 确保返回 true 表示 token 已成功加入黑名单
        return true;
    }

    /**
     * 检查 token 是否已失效（是否在黑名单中）。
     *
     * @param uuid JWT 的唯一标识符
     * @return 如果存在于黑名单中返回 true，否则返回 false
     */
    private boolean isInvalidToken(String uuid) {
        // 判断 Redis 中是否存在该 token，如果存在说明已失效
        // Boolean.TRUE.equals 是 Java 标准库中 Boolean 类的方法，用于比较两个布尔值是否相等。安全地比较布尔值，避免空指针异常 (NullPointerException)。
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }



}
