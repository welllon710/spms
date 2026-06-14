package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.wms.entity.OutputEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface OutputMapper extends BaseMapper<OutputEntity> {
    IPage<OutputEntity> getPageList(
            Page<OutputEntity> page,
            @Param("params") Map<String, Object> params
    );

    OutputEntity getById(@Param("id") Long id);
}
