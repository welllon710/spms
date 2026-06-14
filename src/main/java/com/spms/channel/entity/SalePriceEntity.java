package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("sale_price")
public class SalePriceEntity extends BaseEntity {
    private Long customerId;
    private Long materialId;
    private BigDecimal price;

    @TableField(exist = false)
    private CustomerEntity customer;

    @TableField(exist = false)
    private MaterialEntity material;
}
