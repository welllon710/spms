package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.base.SortParam;
import com.spms.channel.entity.SupplierEntity;
import com.spms.channel.mapper.SupplierMapper;
import com.spms.channel.model.SupplierPageFilter;
import com.spms.channel.service.SupplierService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends BaseService<SupplierEntity> implements SupplierService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");

    private final SupplierMapper supplierMapper;

    @Override
    public PageResult<SupplierEntity> getPage(PageQuery<SupplierPageFilter> request) {
        SupplierPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", SupplierPageFilter::name)
                .putTrim("code", SupplierPageFilter::code)
                .putTrim("phone", SupplierPageFilter::phone)
                .put("isDisabled", SupplierPageFilter::isDisabled)
                .toMap();
        Page<SupplierEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(supplierMapper.selectPage(page, buildPageWrapper(params)), DEFAULT_SORT);
    }

    @Override
    public SupplierEntity getDetail(Long id) {
        return getRequiredSupplier(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity add(SupplierEntity supplier) {
        requireNotNull(supplier, "请求参数不能为空");
        requireText(supplier.getName(), "供应商名称不能为空");
        checkDuplicate(supplier.getName(), supplier.getCode(), null);
        supplierMapper.insert(supplier);
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity update(SupplierEntity supplier) {
        validateSupplier(supplier, true);
        SupplierEntity exist = getRequiredSupplier(supplier.getId());
        checkEditable(exist);
        checkDuplicate(supplier.getName(), supplier.getCode(), supplier.getId());
        if (supplier.getIsDisabled() == null) {
            supplier.setIsDisabled(exist.getIsDisabled());
        }
        supplierMapper.updateById(supplier);
        return getDetail(supplier.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SupplierEntity exist = getRequiredSupplier(id);
        checkEditable(exist);
        supplierMapper.deleteById(id);
    }

    private SupplierEntity getRequiredSupplier(Long id) {
        requireId(id, "供应商ID不能为空");
        SupplierEntity supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "供应商不存在");
        }
        return supplier;
    }

    private void validateSupplier(SupplierEntity supplier, boolean requireId) {
        requireNotNull(supplier, "请求参数不能为空");
        if (requireId) {
            requireId(supplier.getId(), "供应商ID不能为空");
        }
        requireText(supplier.getName(), "供应商名称不能为空");
        supplier.setName(trimToNull(supplier.getName()));
        supplier.setCode(trimToNull(supplier.getCode()));
        supplier.setPhone(trimToNull(supplier.getPhone()));
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        LambdaQueryWrapper<SupplierEntity> wrapper = Wrappers.lambdaQuery(SupplierEntity.class)
                .and(query -> query.eq(SupplierEntity::getName, name).or().eq(SupplierEntity::getCode, code));
        if (excludeId != null) {
            wrapper.ne(SupplierEntity::getId, excludeId);
        }
        if (supplierMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "供应商名称或编码已存在");
        }
    }

    private LambdaQueryWrapper<SupplierEntity> buildPageWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");
        String code = (String) params.get("code");
        String phone = (String) params.get("phone");
        Boolean isDisabled = (Boolean) params.get("isDisabled");
        return Wrappers.lambdaQuery(SupplierEntity.class)
                .like(name != null, SupplierEntity::getName, name)
                .like(code != null, SupplierEntity::getCode, code)
                .like(phone != null, SupplierEntity::getPhone, phone)
                .eq(isDisabled != null, SupplierEntity::getIsDisabled, isDisabled)
                .orderByDesc(SupplierEntity::getId);
    }
}
