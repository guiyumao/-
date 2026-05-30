package edu.university.lab.module.consumableinbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.consumableinbound.entity.ConsumableInbound;
import edu.university.lab.module.consumableinbound.mapper.ConsumableInboundMapper;
import edu.university.lab.module.consumableinbound.service.ConsumableInboundService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumableInboundServiceImpl extends BaseCrudService<ConsumableInboundMapper, ConsumableInbound> implements ConsumableInboundService {

    @Override
    public Page<ConsumableInbound> pageQuery(PageQuery query) {
        return pageQuery(query, new LambdaQueryWrapper<ConsumableInbound>().orderByDesc(ConsumableInbound::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumableInbound createInbound(ConsumableInbound inbound) {
        BigDecimal totalAmount = inbound.getUnitPrice().multiply(inbound.getQuantity());
        inbound.setTotalAmount(totalAmount);
        save(inbound);
        return inbound;
    }
}
