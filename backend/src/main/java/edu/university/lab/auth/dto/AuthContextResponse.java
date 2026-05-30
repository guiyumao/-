package edu.university.lab.auth.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 认证上下文响应
 */
@Data
@Builder
public class AuthContextResponse {

    private UserProfile user;

    private List<MenuItem> menus;

    private List<String> permissions;
}
