package edu.university.lab.module.laboratory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.laboratory.entity.Laboratory;
import edu.university.lab.module.laboratory.mapper.LaboratoryMapper;
import edu.university.lab.module.laboratory.service.LaboratoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LaboratoryServiceImpl extends BaseCrudService<LaboratoryMapper, Laboratory> implements LaboratoryService {

    @Override
    public Page<Laboratory> pageQuery(PageQuery query) {
        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<Laboratory>()
            .orderByDesc(Laboratory::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Laboratory::getLaboratoryName, query.getKeyword())
                .or()
                .like(Laboratory::getLaboratoryCode, query.getKeyword())
                .or()
                .like(Laboratory::getLocation, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
