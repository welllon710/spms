package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("customer")
public class CustomerEntity extends BaseEntity {
    private String name;
    private String code;
    private String phone;
}
