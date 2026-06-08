package com.spms.personal.mapper;

import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMapper {
    List<RoleEntity> getPageList(Map<String, Object> params);

    RoleEntity getById(@Param("id") Long id);

    int countByNameOrCode(
            @Param("name") String name,
            @Param("code") String code,
            @Param("excludeId") Long excludeId
    );

    int countUserRoleByRoleId(@Param("roleId") Long roleId);

    int insert(RoleEntity role);

    int update(RoleEntity role);

    int deleteMenuRelationsByRoleId(@Param("roleId") Long roleId);

    int deletePermissionRelationsByRoleId(@Param("roleId") Long roleId);

    int deleteById(@Param("id") Long id);

    List<MenuEntity> getMenuListByRoleId(@Param("roleId") Long roleId);

    List<PermissionEntity> getPermissionListByRoleId(@Param("roleId") Long roleId);
}
