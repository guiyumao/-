package edu.university.lab.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull
    private Integer laboratoryId;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    @Size(max = 64)
    private String realName;

    @NotBlank
    @Size(max = 50)
    private String userNo;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 100)
    private String email;

    @NotNull
    @Pattern(regexp = "teacher|student")
    private String registerType;
}
