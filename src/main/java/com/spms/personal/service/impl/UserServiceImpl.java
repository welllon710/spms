package com.spms.personal.service.impl;


import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.redis.RedisHelper;
import com.spms.common.result.Json;
import com.spms.common.security.LoginSessionService;
import com.spms.common.security.PermissionUtil;
import com.spms.common.security.TokenService;
import com.spms.common.util.TreeUtils;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.mapper.UserMapper;
import com.spms.personal.model.UserLoginRequest;
import com.spms.personal.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Resource
    private  UserMapper userMapper;

    @Resource
    private  TokenService tokenService;

    @Resource
    private LoginSessionService loginSessionService;

    @Resource
    private RedisHelper redisHelper;

    private @NotNull String getUserPermissionCacheKey(long userId) {
        return "user_permission_" + userId;
    }

    private @NotNull String getUserMenuCacheKey(long userId) {
        return "user_menu_" + userId;
    }

    @Override
    public String login(UserLoginRequest request) {
        if (request == null) {
            throw new AppException(CommonError.PARAM_MISSING, "请求参数不能为空");
        }
        String email = request.email();
        String password = request.password();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new AppException(CommonError.PARAM_MISSING, "邮箱,密码不能为空");
        }
        email = email.trim();

        UserEntity userEntity = userMapper.getByEmail(email);
        if (userEntity == null || !isPasswordMatched(password, userEntity)) {
            throw new AppException(CommonError.UNAUTHORIZED, "邮箱,或密码错误");
        }
        if (Boolean.TRUE.equals(userEntity.getIsDisabled())) {
            throw new AppException(CommonError.FORBIDDEN, "账号已禁用");
        }
        redisHelper.delete(getUserPermissionCacheKey(userEntity.getId()));
        redisHelper.delete(getUserMenuCacheKey(userEntity.getId()));
        String token = tokenService.create(userEntity.getId());
        loginSessionService.save(userEntity.getId(), token);
        return token;
    }

    @Override
    public void logout(long currentUserId) {
        loginSessionService.delete(currentUserId);
        redisHelper.delete(getUserPermissionCacheKey(currentUserId));
        redisHelper.delete(getUserMenuCacheKey(currentUserId));
    }

    @Override
    public List<String> getMyPermissionList(long currentUserId) {
        String userPermissionCacheKey = getUserPermissionCacheKey(currentUserId);
        Object permissionIdentityList = redisHelper.get(userPermissionCacheKey);
        if (Objects.nonNull(permissionIdentityList)) {
            try {
                return Json.parseList(permissionIdentityList.toString(), String[].class);
            } catch (IllegalArgumentException exception) {
                redisHelper.delete(userPermissionCacheKey);
            }
        }
        UserEntity userEntity = userMapper.getById(currentUserId);
        List<String> permissionList;
        if (userEntity == null) {
            throw new AppException(CommonError.UNAUTHORIZED);
        }
        if (userEntity.isRootUser()) {
            permissionList = userMapper.getAllPermissionIdentityList();
        } else  {
            permissionList = userMapper.getPermissionIdentityListByUserId(currentUserId);
        }
        redisHelper.set(userPermissionCacheKey, permissionList);
        return permissionList;
    }

    @Override
    public List<MenuEntity> getMyMenuList(long currentUserId) {
        String userMenuCacheKey = getUserMenuCacheKey(currentUserId);
        Object menu = redisHelper.get(userMenuCacheKey);
        if (Objects.nonNull(menu)) {
            return Json.parseList(menu.toString(), MenuEntity[].class);
        }
        UserEntity userEntity = userMapper.getById(currentUserId);
        if (userEntity == null) {
            throw new AppException(CommonError.UNAUTHORIZED);
        }
        List<MenuEntity> menuList;
        if (userEntity.isRootUser()) {
            menuList = userMapper.getAllMenuList();
        } else {
            menuList = userMapper.getMenuListByUserId(currentUserId);
        }
        List<MenuEntity> menuEntities = TreeUtils.buildMenuTree(menuList);
        redisHelper.set(userMenuCacheKey, Json.toString(menuEntities));
        return menuEntities;
    }

    @Override
    public UserEntity getMyInfo(long currentUserId) {
        return userMapper.getById(currentUserId);
    }

    private boolean isPasswordMatched(String password, UserEntity userEntity) {
        if (!StringUtils.hasText(userEntity.getSalt()) || !StringUtils.hasText(userEntity.getPassword())) {
            return false;
        }
        String encodedPassword = PermissionUtil.encodePassword(password, userEntity.getSalt());
        return encodedPassword.equalsIgnoreCase(userEntity.getPassword());
    }

    
}
