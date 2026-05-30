package edu.university.lab.module.hazardousmaterial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.hazardousmaterial.entity.HazardousMaterial;

public interface HazardousMaterialService extends IService<HazardousMaterial> {

    Page<HazardousMaterial> pageQuery(PageQuery query);
}
