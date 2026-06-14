package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.wms.entity.MoveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MoveMapper extends BaseMapper<MoveEntity> {
    IPage<MoveEntity> getPageList(
            Page<MoveEntity> page,
            @Param("params") Map<String, Object> params
    );

    MoveEntity getById(@Param("id") Long id);
}
