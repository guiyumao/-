package edu.university.lab.auth.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户信息
 */
@Data
@Builder
public class UserProfile {

    private Integer id;

    private Integer laboratoryId;

    private String username;

    private String realName;

    private Integer userType;

    private List<String> roleCodes;
}
