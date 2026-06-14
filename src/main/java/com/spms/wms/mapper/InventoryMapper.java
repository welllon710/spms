package com.spms.wms.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.wms.entity.InventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryEntity> {

    IPage<InventoryEntity> getPageList(
            Page<InventoryEntity> page,
            @Param("params") Map<String, Object> params
    );

    /**
     * 原子增加库存数量。
     * 安全前提：(material_id, storage_id, type) 上存在唯一索引。
     * @return 影响行数，0 表示记录不存在
     */
    @Update("UPDATE inventory SET quantity = quantity + #{quantity}, update_time = #{updateTime} " +
            "WHERE material_id = #{materialId} AND storage_id = #{storageId} AND type = #{type}")
    int increaseQuantity(@Param("materialId") Long materialId,
                         @Param("storageId") Long storageId,
                         @Param("type") int type,
                         @Param("quantity") BigDecimal quantity,
                         @Param("updateTime") long updateTime);

    /**
     * 原子减少库存数量，仅在库存充足（quantity >= amount）时成功。
     * @return 影响行数，0 表示库存不足
     */
    @Update("UPDATE inventory SET quantity = quantity - #{quantity}, update_time = #{updateTime} " +
            "WHERE id = #{id} AND quantity >= #{quantity}")
    int decreaseQuantity(@Param("id") Long id,
                         @Param("quantity") BigDecimal quantity,
                         @Param("updateTime") long updateTime);

    /**
     * 原子移库：减少来源库存，仅在来源充足时成功。
     * 安全前提：(material_id, storage_id, type) 上存在唯一索引。
     * @return 影响行数，0 表示来源库存不足
     */
    @Update("UPDATE inventory SET quantity = quantity - #{quantity}, update_time = #{updateTime} " +
            "WHERE id = #{sourceId} AND quantity >= #{quantity}")
    int decreaseForMove(@Param("sourceId") Long sourceId,
                        @Param("quantity") BigDecimal quantity,
                        @Param("updateTime") long updateTime);

    /**
     * 原子增加目标库存数量（移库目标侧）。
     * 安全前提：(material_id, storage_id, type) 上存在唯一索引。
     * @return 影响行数，0 表示记录不存在
     */
    @Update("UPDATE inventory SET quantity = quantity + #{quantity}, update_time = #{updateTime} " +
            "WHERE material_id = #{materialId} AND storage_id = #{storageId} AND type = #{type}")
    int increaseForMove(@Param("materialId") Long materialId,
                        @Param("storageId") Long storageId,
                        @Param("type") int type,
                        @Param("quantity") BigDecimal quantity,
                        @Param("updateTime") long updateTime);

}
