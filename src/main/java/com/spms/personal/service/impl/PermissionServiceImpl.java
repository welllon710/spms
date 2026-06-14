package com.spms.personal.service.impl;

import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.QueryParams;
import com.spms.common.util.TreeUtils;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.mapper.PermissionMapper;
import com.spms.personal.model.PermissionPageFilter;
import com.spms.personal.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends BaseService<PermissionEntity> implements PermissionService {
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionEntity> getPage(PageQuery<PermissionPageFilter> request) {
        PermissionPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("identity", PermissionPageFilter::identity)
                .putTrim("name", PermissionPageFilter::name)
                .put("parentId", PermissionPageFilter::parentId)
                .put("type", PermissionPageFilter::type)
                .put("isSystem", PermissionPageFilter::isSystem)
                .put("isDisabled", PermissionPageFilter::isDisabled)
                .toMap();
        List<PermissionEntity> pageList = permissionMapper.getPageList(params);
        return TreeUtils.buildTree(
                pageList,
                PermissionEntity::getId,
                PermissionEntity::getParentId,
                PermissionEntity::setChildren
        );
    }

    @Override
    public PermissionEntity getDetail(Long id) {
        return getRequiredPermission(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionEntity add(PermissionEntity permission) {
        validatePermission(permission, false);
        permission.setIsSystem(Boolean.TRUE.equals(permission.getIsSystem()));
        checkParentExists(permission.getParentId(), null);
        checkDuplicate(permission.getIdentity(), permission.getName(), null);
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionEntity update(PermissionEntity permission) {
        validatePermission(permission, true);
        PermissionEntity exist = getRequiredPermission(permission.getId());
        checkEditable(exist);
        checkParentExists(permission.getParentId(), permission.getId());
        checkDuplicate(permission.getIdentity(), permission.getName(), permission.getId());
        if (permission.getIsDisabled() == null) {
            permission.setIsDisabled(exist.getIsDisabled());
        }
        if (permission.getIsSystem() == null) {
            permission.setIsSystem(exist.getIsSystem());
        }
        permissionMapper.update(permission);
        return getDetail(permission.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PermissionEntity exist = getRequiredPermission(id);
        checkEditable(exist);
        if (Boolean.TRUE.equals(exist.getIsSystem())) {
            throw new AppException(CommonError.FORBIDDEN, "系统权限不能删除");
        }
        if (permissionMapper.countByParentId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "权限存在子权限，不能删除");
        }
        if (permissionMapper.countRolePermissionByPermissionId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "权限正在被角色使用，不能删除");
        }
        permissionMapper.deleteById(id);
    }

    private PermissionEntity getRequiredPermission(Long id) {
        requireId(id, "权限ID不能为空");
        PermissionEntity permission = permissionMapper.getById(id);
        if (permission == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "权限不存在");
        }
        return permission;
    }

    private void validatePermission(PermissionEntity permission, boolean requireId) {
        requireNotNull(permission, "请求参数不能为空");
        if (requireId && permission.getId() == null) {
            throw new AppException(CommonError.PARAM_MISSING, "权限ID不能为空");
        }
        permission.setIdentity(trimToNull(permission.getIdentity()));
        permission.setName(trimToNull(permission.getName()));
        requireText(permission.getIdentity(), "权限标识不能为空");
        requireText(permission.getName(), "权限名称不能为空");
    }

    private void checkParentExists(Long parentId, Long currentPermissionId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (Objects.equals(parentId, currentPermissionId)) {
            throw new AppException(CommonError.PARAM_INVALID, "上级权限不能是自己");
        }
        if (permissionMapper.getById(parentId) == null) {
            throw new AppException(CommonError.PARAM_INVALID, "上级权限不存在");
        }
    }

    private void checkDuplicate(String identity, String name, Long excludeId) {
        if (permissionMapper.countByIdentityOrName(identity, name, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "权限标识或名称已存在");
        }
    }
}
