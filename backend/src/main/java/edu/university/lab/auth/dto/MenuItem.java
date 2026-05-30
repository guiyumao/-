package edu.university.lab.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 菜单项
 */
@Data
@Builder
public class MenuItem {

    private Integer id;

    private Integer parentId;

    private String path;

    private String label;

    private String permission;

    private String component;

    private Integer sortOrder;

    private String icon;
}
