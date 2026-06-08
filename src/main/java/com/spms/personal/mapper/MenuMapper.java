package com.spms.personal.mapper;

import com.spms.personal.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MenuMapper {
    List<MenuEntity> getPageList(Map<String, Object> params);

    MenuEntity getById(@Param("id") Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int countByParentId(@Param("parentId") Long parentId);

    int countRoleMenuByMenuId(@Param("menuId") Long menuId);

    int insert(MenuEntity menu);

    int update(MenuEntity menu);

    int deleteById(@Param("id") Long id);
}
