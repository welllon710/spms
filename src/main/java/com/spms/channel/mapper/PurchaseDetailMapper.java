package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.channel.entity.PurchaseDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseDetailMapper extends BaseMapper<PurchaseDetailEntity>  {

    //自定义sql
    List<PurchaseDetailEntity> getPurchaseDetailList(@Param("billId") Long billId);

    @Select("SELECT COUNT(*) FROM purchase_detail WHERE bill_id = #{billId} AND is_finished = 0")
    int countUnfinished(@Param("billId") Long billId);
}
