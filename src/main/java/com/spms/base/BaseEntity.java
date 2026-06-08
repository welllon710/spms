package com.spms.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEntity {
    private Long id;
    private Long createTime;
    private Long updateTime;
    private Boolean isDisabled;
    private Boolean isPublished;
}
