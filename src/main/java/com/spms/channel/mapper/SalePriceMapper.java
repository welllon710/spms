package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.entity.SalePriceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalePriceMapper extends BaseMapper<SalePriceEntity> {
    IPage<SalePriceEntity> getPageList(Page<SalePriceEntity> page, Object o);

    SalePriceEntity getById(String id);
}
