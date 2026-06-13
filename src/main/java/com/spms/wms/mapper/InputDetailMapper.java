package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.wms.entity.InputDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InputDetailMapper extends BaseMapper<InputDetailEntity> {
    List<InputDetailEntity> getByBillId(@Param("billId") Long billId);
}
