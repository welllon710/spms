package com.spms.personal.service.impl;

import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.QueryParams;
import com.spms.common.util.TreeUtils;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.mapper.DepartmentMapper;
import com.spms.personal.model.DepartmentPageFilter;
import com.spms.personal.service.DepartmentService;
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
public class DepartmentServiceImpl extends BaseService<DepartmentEntity> implements DepartmentService {
    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentEntity> getPage(PageQuery<DepartmentPageFilter> request) {
        DepartmentPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", DepartmentPageFilter::name)
                .putTrim("code", DepartmentPageFilter::code)
                .put("parentId", DepartmentPageFilter::parentId)
                .put("isDisabled", DepartmentPageFilter::isDisabled)
                .toMap();
        return TreeUtils.buildTree(
                departmentMapper.getPageList(params),
                DepartmentEntity::getId,
                DepartmentEntity::getParentId,
                DepartmentEntity::setChildren
        );
    }

    @Override
    public DepartmentEntity getDetail(Long id) {
        return getRequiredDepartment(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentEntity add(DepartmentEntity department) {
        validateDepartment(department, false);
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
