package edu.university.lab.module.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.module.menu.dto.MenuTreeItem;
import edu.university.lab.module.menu.entity.Menu;
import edu.university.lab.module.menu.mapper.MenuMapper;
import edu.university.lab.module.menu.service.MenuService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<MenuTreeItem> tree() {
        List<MenuTreeItem> items = list(new LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getSortOrder)
                .orderByAsc(Menu::getId))
            .stream()
            .map(menu -> MenuTreeItem.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .routePath(menu.getRoutePath())
                .permissionCode(menu.getPermissionCode())
                .menuType(menu.getMenuType())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .build())
            .toList();

        Map<Integer, MenuTreeItem> itemMap = items.stream()
            .collect(Collectors.toMap(MenuTreeItem::getId, Function.identity()));

        List<MenuTreeItem> roots = items.stream()
            .filter(item -> item.getParentId() == null || item.getParentId() == 0)
            .sorted(Comparator.comparing(item -> item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder()))
            .toList();

        items.stream()
            .filter(item -> item.getParentId() != null && item.getParentId() != 0)
            .forEach(item -> {
                MenuTreeItem parent = itemMap.get(item.getParentId());
                if (parent != null) {
                    parent.getChildren().add(item);
                    parent.getChildren().sort(Comparator.comparing(child -> child.getSortOrder() == null ? Integer.MAX_VALUE : child.getSortOrder()));
                }
            });

        return roots;
    }
}
