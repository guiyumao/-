package edu.university.lab.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotNull
    private Integer laboratoryId;

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    private String initialPassword;

    @NotBlank
    @Size(max = 64)
    private String realName;

    @Size(max = 64)
    private String userNo;

    @Size(max = 32)
    private String phone;

    @Size(max = 128)
    private String email;

    @NotNull
    private Integer userType;

    @NotNull
    private Integer status;

    private List<Integer> roleIds;
}
