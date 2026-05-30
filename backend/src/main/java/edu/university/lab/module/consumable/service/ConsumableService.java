package edu.university.lab.module.consumable.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumable.entity.Consumable;

public interface ConsumableService extends IService<Consumable> {

    Page<Consumable> pageQuery(PageQuery query);
}
