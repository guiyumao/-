package edu.university.lab.module.consumable.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.consumable.entity.Consumable;
import edu.university.lab.module.consumable.mapper.ConsumableMapper;
import edu.university.lab.module.consumable.service.ConsumableService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConsumableServiceImpl extends BaseCrudService<ConsumableMapper, Consumable> implements ConsumableService {

    @Override
    public Page<Consumable> pageQuery(PageQuery query) {
        LambdaQueryWrapper<Consumable> wrapper = new LambdaQueryWrapper<Consumable>()
            .orderByDesc(Consumable::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Consumable::getConsumableName, query.getKeyword())
                .or()
                .like(Consumable::getConsumableCode, query.getKeyword())
                .or()
                .like(Consumable::getSpecification, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
