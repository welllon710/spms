package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("purchase")
public class PurchaseEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private String reason;
    private Integer status;
    private Double totalPrice;
    private Double totalRealPrice;

    @TableField(exist = false)
    private List<PurchaseDetailEntity> detailList;
}
