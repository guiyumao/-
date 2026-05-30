package edu.university.lab.module.hazardousmaterial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;
import edu.university.lab.module.hazardousmaterial.mapper.HazardousMaterialMapper;
import edu.university.lab.module.hazardousmaterial.service.HazardousMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HazardousMaterialServiceImpl extends BaseCrudService<HazardousMaterialMapper, HazardousMaterial> implements HazardousMaterialService {

    @Override
    public Page<HazardousMaterial> pageQuery(PageQuery query) {
        LambdaQueryWrapper<HazardousMaterial> wrapper = new LambdaQueryWrapper<HazardousMaterial>()
            .orderByDesc(HazardousMaterial::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(HazardousMaterial::getMaterialName, query.getKeyword())
                .or()
                .like(HazardousMaterial::getHazardousCode, query.getKeyword())
                .or()
                .like(HazardousMaterial::getCasNo, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
