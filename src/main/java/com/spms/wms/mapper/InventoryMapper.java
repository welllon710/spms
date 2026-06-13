package com.spms.wms.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.wms.entity.InventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryEntity> {

    IPage<InventoryEntity> getPageList(
            Page<InventoryEntity> page,
            @Param("params") Map<String, Object> params
    );

}
