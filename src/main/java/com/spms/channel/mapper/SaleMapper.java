package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.channel.entity.SaleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface SaleMapper extends BaseMapper<SaleEntity> {
    IPage<SaleEntity> getPageList(
            Page<SaleEntity> page,
            @Param("params") Map<String, Object> params
    );

    SaleEntity getById(@Param("id") Long id);
}
