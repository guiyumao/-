package edu.university.lab.module.menu.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.module.menu.dto.MenuTreeItem;
import edu.university.lab.module.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Menu")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单树")
    @PreAuthorize("hasAuthority('menu:view')")
    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeItem>> tree() {
        return ApiResponse.success(menuService.tree());
    }
}
