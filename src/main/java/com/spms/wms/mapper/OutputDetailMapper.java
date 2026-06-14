package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.wms.entity.OutputDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutputDetailMapper extends BaseMapper<OutputDetailEntity> {
    List<OutputDetailEntity> getByBillId(@Param("billId") Long billId);
}
