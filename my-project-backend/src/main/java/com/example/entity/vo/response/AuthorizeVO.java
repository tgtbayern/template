package com.example.entity.vo.response;

import lombok.Data;
import lombok.Generated;

import java.util.Date;
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}

