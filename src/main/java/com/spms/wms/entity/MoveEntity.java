package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@TableName("move")
public class MoveEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private Integer status;
    @NotNull(message = "目标仓库不能为空")
    private Long storageId;

    @TableField(exist = false)
    private StorageEntity storage;

    @Valid
    @NotEmpty(message = "移库明细不能为空")
    @TableField(exist = false)
    private List<MoveDetailEntity> details;
}
