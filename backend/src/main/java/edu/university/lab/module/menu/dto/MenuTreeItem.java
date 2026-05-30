package edu.university.lab.module.menu.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuTreeItem {

    private Integer id;

    private Integer parentId;

    private String menuCode;

    private String menuName;

    private String routePath;

    private String permissionCode;

    private Integer menuType;

    private Integer sortOrder;

    private Integer visible;

    private Integer status;

    @Builder.Default
    private List<MenuTreeItem> children = new ArrayList<>();
}
