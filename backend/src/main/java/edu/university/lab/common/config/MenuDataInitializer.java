package edu.university.lab.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.lab.module.menu.entity.Menu;
import edu.university.lab.module.menu.mapper.MenuMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
@RequiredArgsConstructor
public class MenuDataInitializer implements ApplicationRunner {

    private final MenuMapper menuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>());
        Map<String, String> expectedLabels = new HashMap<>();
        expectedLabels.put("/dashboard", "仪表盘");
        expectedLabels.put("/reminders", "到期提醒");
        expectedLabels.put("/laboratories", "实验室管理");
        expectedLabels.put("/users", "用户管理");
        expectedLabels.put("/roles", "角色与菜单");
        expectedLabels.put("/audit-logs", "操作日志");
        expectedLabels.put("/equipment-categories", "设备分类");
        expectedLabels.put("/equipment", "设备台账");
        expectedLabels.put("/equipment-borrows", "设备借用");
        expectedLabels.put("/equipment-repairs", "设备维修");
        expectedLabels.put("/equipment-calibrations", "设备校准");
        expectedLabels.put("/consumable-categories", "耗材分类");
        expectedLabels.put("/consumables", "耗材台账");
        expectedLabels.put("/consumable-inbounds", "耗材入库");
        expectedLabels.put("/consumable-outbounds", "耗材出库");
        expectedLabels.put("/hazardous-materials", "危化品台账");
        expectedLabels.put("/hazardous-usages", "危化品领用");
        expectedLabels.put("/inventory/alert", "库存预警");
        expectedLabels.put("/reports", "报表中心");
        expectedLabels.put("/system", "系统管理");
        expectedLabels.put("/business", "业务流程");
        expectedLabels.put("/assets", "资产档案");
        expectedLabels.put("/inventory", "库存管理");

        for (Menu menu : menus) {
            if (menu == null || menu.getRoutePath() == null) {
                continue;
            }
            if (isBadLabel(menu.getMenuName())) {
                String label = expectedLabels.get(menu.getRoutePath());
                if (label != null && !label.equals(menu.getMenuName())) {
                    menu.setMenuName(label);
                    menuMapper.updateById(menu);
                }
            }
        }
    }

    private boolean isBadLabel(String label) {
        return label == null || label.isBlank() || "???".equals(label) || label.chars().allMatch(ch -> ch == '?');
    }
}
