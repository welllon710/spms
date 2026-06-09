package com.spms.personal.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMapper {
    IPage<RoleEntity> getPageList(Page<RoleEntity> page, @Param("params") Map<String, Object> params);

    RoleEntity getById(@Param("id") Long id);

    String getLatestGeneratedRoleCodeForUpdate(@Param("prefix") String prefix);

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

    void authorizeMenu(Long id, List<MenuEntity> menuList);

    void deleteAuthorizeMenu(Long id);

    List<RoleEntity> getRoleByUserId(@Param("userId") Long id);

    Integer updateUserRoleList(@Param("id") Long userId, @Param("roleIds") List<RoleEntity> roleIds);

    void deleteUserRoleList(@Param("id") Long userId);
}
