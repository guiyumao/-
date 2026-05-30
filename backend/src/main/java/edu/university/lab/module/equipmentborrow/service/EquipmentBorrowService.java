package edu.university.lab.module.equipmentborrow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.equipmentborrow.entity.EquipmentBorrow;

public interface EquipmentBorrowService extends IService<EquipmentBorrow> {

    Page<EquipmentBorrow> pageQuery(PageQuery query);
}
