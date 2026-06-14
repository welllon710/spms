package com.spms.personal.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.redis.RedisHelper;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.LoginSessionService;
import com.spms.common.security.PermissionUtil;
import com.spms.common.security.TokenService;
import com.spms.common.util.QueryParams;
import com.spms.common.util.TreeUtils;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.mapper.DepartmentMapper;
import com.spms.personal.mapper.RoleMapper;
import com.spms.personal.mapper.UserMapper;
import com.spms.personal.model.UserLoginRequest;
import com.spms.personal.model.UserPageFilter;
import com.spms.personal.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService<UserEntity> implements UserService {

    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final LoginSessionService loginSessionService;
    private final RedisHelper redisHelper;
    private final RoleMapper roleMapper;
    private final DepartmentMapper departmentMapper;

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
    public void logout(String token) {
        OptionalLong currentUserId = tokenService.tryVerify(token);
        if (currentUserId.isEmpty()) {
            return;
        }
        clearLoginState(currentUserId.getAsLong());
    }

    private void clearLoginState(long currentUserId) {
        redisHelper.delete(getUserPermissionCacheKey(currentUserId));
        redisHelper.delete(getUserMenuCacheKey(currentUserId));
        loginSessionService.delete(currentUserId);
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
        List<MenuEntity> menuEntities = TreeUtils.buildTree(
                menuList,
                MenuEntity::getId,
                MenuEntity::getParentId,
                MenuEntity::setChildren
        );
        redisHelper.set(userMenuCacheKey, Json.toString(menuEntities));
        return menuEntities;
    }

    @Override
    public UserEntity getMyInfo(long currentUserId) {
        UserEntity userEntity = userMapper.getById(currentUserId);
        if (userEntity == null) {
            throw new AppException(CommonError.UNAUTHORIZED);
        }
        return userEntity;
    }

    @Override
    public PageResult<UserEntity> getPage(PageQuery<UserPageFilter> request) {
        UserPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .put("departmentId", UserPageFilter::departmentId)
                .toMap();
        Page<UserEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(userMapper.getPageList(page, params), null);
    }

    @Override
    public UserEntity getDetail(Long id) {
        UserEntity userEntity = getRequiredUser(id);
        List<RoleEntity> roleByUserId = roleMapper.getRoleByUserId(id);
        List<DepartmentEntity> departmentList = departmentMapper.getDepartmentListByUserId(id);
        userEntity.setRoleList(roleByUserId);
        userEntity.setDepartmentList(departmentList);
        return userEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserEntity userEntity) {
        requireNotNull(userEntity, "请求参数不能为空");
        getRequiredUser(userEntity.getId());
        UserEntity entity = new UserEntity();
        entity.setId(userEntity.getId());
        entity.setEmail(userEntity.getEmail());
        entity.setPhone(userEntity.getPhone());
        entity.setNickname(userEntity.getNickname());
        userMapper.update(entity);
        if (userEntity.getRoleList() != null) {
            roleMapper.deleteUserRoleList(userEntity.getId());
            if (!userEntity.getRoleList().isEmpty()) {
                roleMapper.updateUserRoleList(userEntity.getId(), userEntity.getRoleList());
            }
        }
        if (userEntity.getDepartmentList() != null) {
            departmentMapper.deleteUserDepartmentList(userEntity.getId());
            if (!userEntity.getDepartmentList().isEmpty()) {
                departmentMapper.updateUserDepartmentList(userEntity.getId(), userEntity.getDepartmentList());
            }
        }
        redisHelper.delete(getUserPermissionCacheKey(userEntity.getId()));
        redisHelper.delete(getUserMenuCacheKey(userEntity.getId()));
    }

    private UserEntity getRequiredUser(Long id) {
        requireId(id, "用户ID不能为空");
        UserEntity userEntity = userMapper.getById(id);
        if (userEntity == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND);
        }
        return userEntity;
    }

    private boolean isPasswordMatched(String password, UserEntity userEntity) {
        if (!StringUtils.hasText(userEntity.getSalt()) || !StringUtils.hasText(userEntity.getPassword())) {
            return false;
        }
        String encodedPassword = PermissionUtil.encodePassword(password, userEntity.getSalt());
        return encodedPassword.equalsIgnoreCase(userEntity.getPassword());
    }

    
}
