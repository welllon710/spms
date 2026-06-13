package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.wms.entity.InputEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface InputMapper extends BaseMapper<InputEntity> {
    IPage<InputEntity> getPageList(
            Page<InputEntity> page,
            @Param("params") Map<String, Object> params
    );

    InputEntity getById(@Param("id") Long id);
}
