package com.spms.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.wms.entity.MoveDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MoveDetailMapper extends BaseMapper<MoveDetailEntity> {
    List<MoveDetailEntity> getByBillId(@Param("billId") Long billId);

    @Select("SELECT COUNT(*) FROM move_detail WHERE bill_id = #{billId} AND is_finished = 0")
    int countUnfinished(@Param("billId") Long billId);
}
