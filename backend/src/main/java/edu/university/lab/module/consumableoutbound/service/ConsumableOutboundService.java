package edu.university.lab.module.consumableoutbound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumableoutbound.entity.ConsumableOutbound;

public interface ConsumableOutboundService extends IService<ConsumableOutbound> {

    Page<ConsumableOutbound> pageQuery(PageQuery query);

    ConsumableOutbound createOutbound(ConsumableOutbound outbound);
}
