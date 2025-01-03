package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.annotation.Bean;
/**
 * AccountMapper 接口继承自 BaseMapper<Account>，即使接口本身为空，也具有重要意义。
 *
 * 1. 继承 BaseMapper 的作用：
 *    - 自动提供基础的 CRUD 操作：
 *      包括 insert()、deleteById()、updateById()、selectById()、selectList() 等。
 *      无需手动编写 SQL，即可使用这些常见数据库操作方法。
 *    - 减少重复代码，提升开发效率。
 *
 * 2. 定义空接口的原因：
 *    - 代码结构清晰：
 *      AccountMapper 专门处理与 Account 实体相关的数据库操作，逻辑集中，代码可读性和可维护性更强。
 *    - 扩展性：
 *      未来若需要自定义 SQL 查询或其他方法，可直接在 AccountMapper 中添加，无需重新创建接口。
 *      示例：
 *      @Select("SELECT * FROM account WHERE email = #{email}")
 *      Account selectByEmail(String email);
 *
 * 3. 示例：
 *    @Mapper
 *    public interface AccountMapper extends BaseMapper<Account> {
 *        @Select("SELECT * FROM account WHERE status = #{status}")
 *        List<Account> selectByStatus(Integer status);
 *    }
 *
 * 结论：
 * - 空接口继承 BaseMapper 是 MyBatis-Plus 中推荐的开发模式，符合“约定大于配置”的设计理念。
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
