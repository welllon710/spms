package com.spms.personal.service.impl;

import com.github.pagehelper.PageInfo;
import com.spms.base.BaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.mapper.RoleMapper;
import com.spms.personal.model.RolePageRequest;
import com.spms.personal.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseService implements RoleService {
    private final RoleMapper roleMapper;

    @Override
    public PageInfo<RoleEntity> getPage(RolePageRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", trimToNull(request == null ? null : request.name()));
        params.put("code", trimToNull(request == null ? null : request.code()));
        params.put("isDisabled", request == null ? null : request.isDisabled());
        return getPage(request, () -> roleMapper.getPageList(params));
    }

    @Override
    public RoleEntity getDetail(Long id) {
        RoleEntity role = getRequiredRole(id);
        role.setMenuList(roleMapper.getMenuListByRoleId(role.getId()));
        role.setPermissionList(roleMapper.getPermissionListByRoleId(role.getId()));
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleEntity add(RoleEntity role) {
        validateRole(role, false);
        initAddEntity(role);
        checkDuplicate(role.getName(), role.getCode(), null);
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleEntity update(RoleEntity role) {
        validateRole(role, true);
        RoleEntity exist = getRequiredRole(role.getId());
        checkEditable(exist);
        checkDuplicate(role.getName(), role.getCode(), role.getId());
        initUpdateEntity(role);
        if (role.getIsDisabled() == null) {
            role.setIsDisabled(exist.getIsDisabled());
        }
        roleMapper.update(role);
        return getDetail(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RoleEntity exist = getRequiredRole(id);
        checkEditable(exist);
        if (roleMapper.countUserRoleByRoleId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "删除失败，角色正在被用户使用");
        }
        roleMapper.deleteMenuRelationsByRoleId(id);
        roleMapper.deletePermissionRelationsByRoleId(id);
        roleMapper.deleteById(id);
    }

    private RoleEntity getRequiredRole(Long id) {
        requireId(id, "角色ID不能为空");
        RoleEntity role = roleMapper.getById(id);
        if (role == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void validateRole(RoleEntity role, boolean requireId) {
        requireEntity(role);
        if (requireId && role.getId() == null) {
            throw new AppException(CommonError.PARAM_MISSING, "角色ID不能为空");
        }
        role.setName(trimToNull(role.getName()));
        role.setCode(trimToNull(role.getCode()));
        requireText(role.getName(), "角色名称不能为空");
        requireText(role.getCode(), "角色编码不能为空");
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        if (roleMapper.countByNameOrCode(name, code, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "角色名称或编码已存在");
        }
    }

}
