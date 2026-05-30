package edu.university.lab.module.hazardoususage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.hazardoususage.entity.HazardousUsage;

public interface HazardousUsageService extends IService<HazardousUsage> {

    Page<HazardousUsage> pageQuery(PageQuery query);

    HazardousUsage createUsage(HazardousUsage usage);
}
