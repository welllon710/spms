package com.spms.system.entity;

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
@TableName("coderule")
public class CodeRuleEntity extends BaseEntity {
    private Integer currentDay;
    private Integer currentMonth;
    private Integer currentSn;
    private Integer currentYear;
    private Boolean isSystem;
    private String prefix;
    private Integer ruleField;
    private Integer snLength;
    private Integer snType;
    private String template;
}
