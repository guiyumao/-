package edu.university.lab.auth.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResponse {

    private String token;

    private Long expireMinutes;

    private UserProfile user;

    private List<MenuItem> menus;

    private List<String> permissions;
}
