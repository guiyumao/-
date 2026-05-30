package edu.university.lab.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.lab.common.query.PageQuery;

/**
 * 通用 CRUD 服务基类
 */
public abstract class BaseCrudService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    public Page<T> pageQuery(PageQuery query, LambdaQueryWrapper<T> wrapper) {
        return page(new Page<>(query.getCurrent(), query.getPageSize()), wrapper);
    }
}
