package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.channel.entity.SaleDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleDetailMapper extends BaseMapper<SaleDetailEntity> {
    List<SaleDetailEntity> getSaleDetailList(@Param("billId") Long billId);
}
