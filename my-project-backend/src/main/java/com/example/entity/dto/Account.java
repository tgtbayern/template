package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
@Data
//将实体类与数据库中的表进行映射, 可以自动从数据库中读取一条数据并且转化为相应的对象
@TableName("db_account")
//自动生成一个包含类中所有字段的构造函数。
@AllArgsConstructor
public class Account {
    //标识类中的某个字段为数据库表的主键，并指定主键的生成策略。
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}
