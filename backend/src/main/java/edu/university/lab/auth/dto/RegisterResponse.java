package edu.university.lab.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private Integer userId;

    private String username;

    private String realName;

    private String roleCode;

    private String message;
}
