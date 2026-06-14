package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("sale")
public class SaleEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private String reason;
    private Integer status;
    private BigDecimal totalPrice;
    private Long customerId;

    @TableField(exist = false)
    private CustomerEntity customer;

    @Valid
    @NotEmpty(message = "销售明细不能为空")
    @JsonAlias("details")
    @TableField(exist = false)
    private List<SaleDetailEntity> details;
}
