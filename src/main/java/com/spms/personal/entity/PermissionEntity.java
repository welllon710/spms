package com.spms.personal.entity;

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
public class PermissionEntity extends BaseEntity {
    private String identity;
    private String name;
    private Long parentId;
    private Integer type;
    private Boolean isSystem;
    private List<PermissionEntity> children;
}
