package com.spms.personal.service;


import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.model.UserLoginRequest;
import com.spms.personal.model.UserPageFilter;

import java.util.List;

public interface UserService {


    String login(UserLoginRequest request);

    void logout(String token);

    List<String> getMyPermissionList(long currentUserId);

    List<MenuEntity> getMyMenuList(long currentUserId);

    UserEntity getMyInfo(long currentUserId);

    PageResult<UserEntity> getPage(PageQuery<UserPageFilter> pageQuery);

    UserEntity getDetail(Long id);

    void update(UserEntity userEntity);
}
