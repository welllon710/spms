package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("purchase_detail")
public class PurchaseDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private Double finishQuantity;
    private Double price;
    private Double quantity;
    private Long materialId;
    private Long supplierId;

    @TableField(exist = false)
    private MaterialEntity material;

    @TableField(exist = false)
    private SupplierEntity supplier;
}
