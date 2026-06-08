package com.spms.personal.service.impl;

import com.spms.base.BaseService;
import com.spms.base.PageResult;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.personal.dto.AuthorizeMenuDto;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.model.RolePageFilter;
import com.spms.personal.mapper.RoleMapper;
import com.spms.personal.model.RolePageRequest;
import com.spms.personal.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseService implements RoleService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");
    private static final String ROLE_CODE_PREFIX = "RO";
    private static final int ROLE_CODE_SERIAL_LENGTH = 4;

    private final RoleMapper roleMapper;

    @Override
    public PageResult<RoleEntity> getPage(RolePageRequest request) {
        RolePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = new HashMap<>();
        params.put("name", trimToNull(filter == null ? null : filter.name()));
        params.put("code", trimToNull(filter == null ? null : filter.code()));
        params.put("isDisabled", filter == null ? null : filter.isDisabled());
        return getPage(request, () -> roleMapper.getPageList(params), DEFAULT_SORT);
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
        if (!StringUtils.hasText(role.getCode())) {
            role.setCode(generateRoleCode());
        }
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

    @Override
    @Transactional
    public void authorizeMenu(AuthorizeMenuDto dto) {
        Long id = dto.getId();
        List<MenuEntity> menuList = dto.getMenuList();
        if (id == null) {
            throw new AppException(CommonError.PARAM_MISSING, "角色ID不能为空");
        }
        if (menuList == null || menuList.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "菜单列表不能为空");

        }
        roleMapper.deleteAuthorizeMenu(id);
        roleMapper.authorizeMenu(id, menuList);
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
//        requireText(role.getCode(), "角色编码不能为空");
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        if (roleMapper.countByNameOrCode(name, code, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "角色名称或编码已存在");
        }
    }

    private String generateRoleCode() {
        String latestCode = roleMapper.getLatestGeneratedRoleCodeForUpdate(ROLE_CODE_PREFIX);
        int nextSerial = 1;
        if (StringUtils.hasText(latestCode)) {
            nextSerial = Integer.parseInt(latestCode.substring(ROLE_CODE_PREFIX.length())) + 1;
        }
        return ROLE_CODE_PREFIX + String.format("%0" + ROLE_CODE_SERIAL_LENGTH + "d", nextSerial);
    }

}
