package edu.university.lab.module.consumableinbound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.consumableinbound.entity.ConsumableInbound;

public interface ConsumableInboundService extends IService<ConsumableInbound> {

    Page<ConsumableInbound> pageQuery(PageQuery query);

    ConsumableInbound createInbound(ConsumableInbound inbound);
}
