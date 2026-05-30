package edu.university.lab.module.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleSaveRequest {

    @NotBlank
    @Size(max = 64)
    private String roleName;

    @NotBlank
    @Size(max = 64)
    private String roleCode;
}
