package edu.university.lab.module.laboratory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.laboratory.entity.Laboratory;
import java.util.List;

public interface LaboratoryService extends IService<Laboratory> {

    Page<Laboratory> pageQuery(PageQuery query);

    List<Laboratory> listEnabled();
}
