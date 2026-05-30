package edu.university.lab.module.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.module.menu.dto.MenuTreeItem;
import edu.university.lab.module.menu.entity.Menu;
import java.util.List;

public interface MenuService extends IService<Menu> {

    List<MenuTreeItem> tree();
}
