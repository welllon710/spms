package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("storage")
public class StorageEntity extends BaseEntity {
    private String code;
    private String name;
    private Long parentId;

    @TableField(exist = false)
    private StorageEntity parent;

    @TableField(exist = false)
    private List<StorageEntity> children;
}
