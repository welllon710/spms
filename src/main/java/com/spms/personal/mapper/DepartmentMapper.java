package com.spms.personal.mapper;

import com.spms.personal.entity.DepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper {
    List<DepartmentEntity> getPageList(Map<String, Object> params);

    DepartmentEntity getById(@Param("id") Long id);

    int countByNameOrCode(
            @Param("name") String name,
            @Param("code") String code,
            @Param("excludeId") Long excludeId
    );

    int countByParentId(@Param("parentId") Long parentId);

    int countUserDepartmentByDepartmentId(@Param("departmentId") Long departmentId);

    int insert(DepartmentEntity department);

    int update(DepartmentEntity department);

    int deleteById(@Param("id") Long id);
}
