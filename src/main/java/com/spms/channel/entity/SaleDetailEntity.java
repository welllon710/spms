package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("sale_detail")
public class SaleDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private Double finishQuantity;
    private Double price;
    private Double quantity;
    private Long materialId;

    @TableField(exist = false)
    private MaterialEntity material;
}
