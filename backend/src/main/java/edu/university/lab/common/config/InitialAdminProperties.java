package edu.university.lab.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.initial-admin")
public class InitialAdminProperties {

    private boolean enabled;

    private String username;

    private String password;

    private String realName = "系统管理员";

    private String userNo = "admin";
}
