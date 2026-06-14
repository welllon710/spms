package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import com.spms.channel.entity.SaleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("output")
public class OutputEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private Integer status;
    private Integer type;
    private Long moveId;
    private Long pickingId;
    private Long saleId;

    @TableField(exist = false)
    private SaleEntity sale;

    @TableField(exist = false)
    private MoveEntity move;

    @Valid
    @NotEmpty(message = "出库明细不能为空")
    @TableField(exist = false)
    private List<OutputDetailEntity> details;
}
