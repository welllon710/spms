package com.spms.personal.service.impl;

import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.TreeUtils;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.mapper.MenuMapper;
import com.spms.personal.model.MenuPageFilter;
import com.spms.personal.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseService<MenuEntity> implements MenuService {
    private final MenuMapper menuMapper;

    @Override
    public List<MenuEntity> getPage(PageQuery<MenuPageFilter> request) {
        MenuPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = new HashMap<>();
        params.put("name", trimToNull(filter == null ? null : filter.name()));
        params.put("parentId", filter == null ? null : filter.parentId());
        params.put("path", trimToNull(filter == null ? null : filter.path()));
        params.put("component", trimToNull(filter == null ? null : filter.component()));
        params.put("isDisabled", filter == null ? null : filter.isDisabled());
        return TreeUtils.buildMenuTree(menuMapper.getPageList(params));
    }

    @Override
    public MenuEntity getDetail(Long id) {
        return getRequiredMenu(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuEntity add(MenuEntity menu) {
        validateMenu(menu, false);
        initAddEntity(menu);
        checkParentExists(menu.getParentId(), null);
        checkDuplicate(menu.getName(), null);
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuEntity update(MenuEntity menu) {
        validateMenu(menu, true);
        MenuEntity exist = getRequiredMenu(menu.getId());
        checkEditable(exist);
        checkParentExists(menu.getParentId(), menu.getId());
        checkDuplicate(menu.getName(), menu.getId());
        initUpdateEntity(menu);
        if (menu.getIsDisabled() == null) {
            menu.setIsDisabled(exist.getIsDisabled());
        }
        menuMapper.update(menu);
        return getDetail(menu.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MenuEntity exist = getRequiredMenu(id);
        checkEditable(exist);
        if (menuMapper.countByParentId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "菜单存在子菜单，不能删除");
        }
        if (menuMapper.countRoleMenuByMenuId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "菜单正在被角色使用，不能删除");
        }
        menuMapper.deleteById(id);
    }

    private MenuEntity getRequiredMenu(Long id) {
        requireId(id, "菜单ID不能为空");
        MenuEntity menu = menuMapper.getById(id);
        if (menu == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private void validateMenu(MenuEntity menu, boolean requireId) {
        requireNotNull(menu, "请求参数不能为空");
        if (requireId && menu.getId() == null) {
            throw new AppException(CommonError.PARAM_MISSING, "菜单ID不能为空");
        }
        menu.setName(trimToNull(menu.getName()));
        menu.setPath(trimToNull(menu.getPath()));
        menu.setComponent(trimToNull(menu.getComponent()));
        menu.setIcon(trimToNull(menu.getIcon()));
        requireText(menu.getName(), "菜单名称不能为空");
    }

    private void checkParentExists(Long parentId, Long currentMenuId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (Objects.equals(parentId, currentMenuId)) {
            throw new AppException(CommonError.PARAM_INVALID, "上级菜单不能是自己");
        }
        if (menuMapper.getById(parentId) == null) {
            throw new AppException(CommonError.PARAM_INVALID, "上级菜单不存在");
        }
    }

    private void checkDuplicate(String name, Long excludeId) {
        if (menuMapper.countByName(name, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "菜单名称已存在");
        }
    }
}
