package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.channel.entity.PurchaseDetailEntity;
import com.spms.channel.entity.PurchasePriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PurchaseDetailMapper extends BaseMapper<PurchaseDetailEntity>  {

    //自定义sql
    List<PurchaseDetailEntity> getPurchaseDetailList(@Param("billId") Long billId);
}
