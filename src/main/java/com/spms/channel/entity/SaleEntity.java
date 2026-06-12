package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
    private Double totalPrice;
    private Long customerId;

    @TableField(exist = false)
    private CustomerEntity customer;

    @JsonAlias("details")
    @TableField(exist = false)
    private List<SaleDetailEntity> details;
}
