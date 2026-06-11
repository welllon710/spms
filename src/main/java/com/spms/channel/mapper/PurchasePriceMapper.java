package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.channel.entity.PurchasePriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


@Mapper
public interface PurchasePriceMapper extends BaseMapper<PurchasePriceEntity> {
    IPage<PurchasePriceEntity> getPageList(
            Page<PurchasePriceEntity> page,
            @Param("params") Map<String, Object> params
    );
    PurchasePriceEntity getById(@Param("id") String id);

    PurchasePriceEntity getByMaterialAndSupplier(@Param("materialId") Long materialId,  @Param("supplierId") Long supplierId);
}
