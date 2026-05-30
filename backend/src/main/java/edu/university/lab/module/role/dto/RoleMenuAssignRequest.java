package edu.university.lab.module.role.dto;

import java.util.List;
import lombok.Data;

@Data
public class RoleMenuAssignRequest {

    private List<Integer> menuIds;
}
