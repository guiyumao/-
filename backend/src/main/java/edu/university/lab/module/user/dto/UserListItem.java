package edu.university.lab.module.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserListItem {

    private Integer id;

    private Integer laboratoryId;

    private String username;

    private String realName;

    private String userNo;

    private String phone;

    private String email;

    private Integer userType;

    private Integer status;

    private List<Integer> roleIds;

    private List<String> roleCodes;
}
