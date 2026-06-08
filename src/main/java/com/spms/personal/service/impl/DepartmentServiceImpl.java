package com.spms.personal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.mapper.DepartmentMapper;
import com.spms.personal.model.DepartmentPageFilter;
import com.spms.personal.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends BaseService<DepartmentEntity> implements DepartmentService {
    private static final SortParam DEFAULT_SORT = new SortParam("orderNo", "asc");

    private final DepartmentMapper departmentMapper;

    @Override
    public PageResult<DepartmentEntity> getPage(PageQuery<DepartmentPageFilter> request) {
        DepartmentPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = new HashMap<>();
        params.put("name", trimToNull(filter == null ? null : filter.name()));
        params.put("code", trimToNull(filter == null ? null : filter.code()));
        params.put("parentId", filter == null ? null : filter.parentId());
        params.put("isDisabled", filter == null ? null : filter.isDisabled());
        PageHelper.startPage(getPageNum(request), getPageSize(request));
        return PageResult.from(new PageInfo<>(departmentMapper.getPageList(params)), DEFAULT_SORT);
    }

    @Override
    public DepartmentEntity getDetail(Long id) {
        return getRequiredDepartment(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentEntity add(DepartmentEntity department) {
        validateDepartment(department, false);
        initAddEntity(department);
        checkParentExists(department.getParentId(), null);
        checkDuplicate(department.getName(), department.getCode(), null);
        departmentMapper.insert(department);
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentEntity update(DepartmentEntity department) {
        validateDepartment(department, true);
        DepartmentEntity exist = getRequiredDepartment(department.getId());
        checkEditable(exist);
        checkParentExists(department.getParentId(), department.getId());
        checkDuplicate(department.getName(), department.getCode(), department.getId());
        initUpdateEntity(department);
        if (department.getIsDisabled() == null) {
            department.setIsDisabled(exist.getIsDisabled());
        }
        departmentMapper.update(department);
        return getDetail(department.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DepartmentEntity exist = getRequiredDepartment(id);
        checkEditable(exist);
        if (departmentMapper.countByParentId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "部门存在子部门，不能删除");
        }
        if (departmentMapper.countUserDepartmentByDepartmentId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "部门正在被用户使用，不能删除");
        }
        departmentMapper.deleteById(id);
    }

    private DepartmentEntity getRequiredDepartment(Long id) {
        requireId(id, "部门ID不能为空");
        DepartmentEntity department = departmentMapper.getById(id);
        if (department == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "部门不存在");
        }
        return department;
    }

    private void validateDepartment(DepartmentEntity department, boolean requireId) {
        requireNotNull(department, "请求参数不能为空");
        if (requireId && department.getId() == null) {
            throw new AppException(CommonError.PARAM_MISSING, "部门ID不能为空");
        }
        department.setName(trimToNull(department.getName()));
        department.setCode(trimToNull(department.getCode()));
        requireText(department.getName(), "部门名称不能为空");
        requireText(department.getCode(), "部门编码不能为空");
    }

    private void checkParentExists(Long parentId, Long currentDepartmentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (Objects.equals(parentId, currentDepartmentId)) {
            throw new AppException(CommonError.PARAM_INVALID, "上级部门不能是自己");
        }
        if (departmentMapper.getById(parentId) == null) {
            throw new AppException(CommonError.PARAM_INVALID, "上级部门不存在");
        }
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        if (departmentMapper.countByNameOrCode(name, code, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "部门名称或编码已存在");
        }
    }
}
