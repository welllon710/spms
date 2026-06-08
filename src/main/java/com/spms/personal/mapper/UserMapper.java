package com.spms.personal.mapper;


import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from `user` where email = #{email}")
    UserEntity getByEmail(@Param("email") String email);

    UserEntity getById(@Param("id") Long id);

    List<PermissionEntity> getPermissionListByRoleId(@Param("roleId") Long roleId);

    List<String> getAllPermissionIdentityList();

    List<String> getPermissionIdentityListByUserId(@Param("userId") Long userId);

    List<MenuEntity> getAllMenuList();

    List<MenuEntity> getMenuListByUserId(@Param("userId") Long userId);
}
