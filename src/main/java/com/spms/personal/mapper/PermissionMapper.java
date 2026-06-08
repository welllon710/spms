package com.spms.personal.mapper;

import com.spms.personal.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionMapper {
    List<PermissionEntity> getPageList(Map<String, Object> params);

    PermissionEntity getById(@Param("id") Long id);

    int countByIdentityOrName(
            @Param("identity") String identity,
            @Param("name") String name,
            @Param("excludeId") Long excludeId
    );

    int countByParentId(@Param("parentId") Long parentId);

    int countRolePermissionByPermissionId(@Param("permissionId") Long permissionId);

    int insert(PermissionEntity permission);

    int update(PermissionEntity permission);

    int deleteById(@Param("id") Long id);
}
