package com.spms.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.entity.MaterialEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MaterialMapper extends BaseMapper<MaterialEntity> {
    IPage<MaterialEntity> getPageList(
            Page<MaterialEntity> page,
            @Param("params") Map<String, Object> params
    );

    MaterialEntity getById(@Param("id") Long id);
}
