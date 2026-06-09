package com.spms.personal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spms.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEntity extends BaseEntity {
    private String avatar;
    private String email;
    private Integer gender;
    private String idCard;
    private String nickname;
    private String phone;
    private String realName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    private List<RoleEntity> roleList;

    private List<DepartmentEntity> departmentList;


    @JsonIgnore
    public final boolean isRootUser() {
        return Objects.nonNull(getId()) && getId() == 1L;
    }
}
