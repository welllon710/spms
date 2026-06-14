package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import com.spms.channel.entity.PurchaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("input")
public class InputEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private Integer status;
    private Integer type;
    private Long moveId;
    private Long orderId;
    private Long purchaseId;
    @Positive(message = "移库单关联ID须大于0")
    private Long structureId;

    @TableField(exist = false)
    private PurchaseEntity purchase;

    @TableField(exist = false)
    private MoveEntity move;

    @Valid
    @NotEmpty(message = "入库明细不能为空")
    @TableField(exist = false)
    private List<InputDetailEntity> details;
}
