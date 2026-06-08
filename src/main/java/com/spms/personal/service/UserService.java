package com.spms.personal.service;


import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.model.UserLoginRequest;

import java.util.List;

public interface UserService {


    String login(UserLoginRequest request);

    void logout(long currentUserId);

    List<String> getMyPermissionList(long currentUserId);

    List<MenuEntity> getMyMenuList(long currentUserId);

    UserEntity getMyInfo(long currentUserId);
}
