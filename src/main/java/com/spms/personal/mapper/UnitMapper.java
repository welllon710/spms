package com.spms.personal.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.personal.entity.UnitEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UnitMapper {
    IPage<UnitEntity> getPageList(Page<UnitEntity> page, @Param("params") Map<String, Object> params);

    UnitEntity getById(@Param("id") Long id);

    int countByNameOrCode(
            @Param("name") String name,
            @Param("code") String code,
            @Param("excludeId") Long excludeId
    );

    int insert(UnitEntity unit);

    int update(UnitEntity unit);

    int deleteById(@Param("id") Long id);
}
