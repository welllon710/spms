package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.base.SortParam;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.mapper.CustomerMapper;
import com.spms.channel.model.CustomerPageFilter;
import com.spms.channel.service.CustomerService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends BaseService<CustomerEntity> implements CustomerService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");

    private final CustomerMapper customerMapper;

    @Override
    public PageResult<CustomerEntity> getPage(PageQuery<CustomerPageFilter> request) {
        CustomerPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", CustomerPageFilter::name)
                .putTrim("code", CustomerPageFilter::code)
                .putTrim("phone", CustomerPageFilter::phone)
                .put("isDisabled", CustomerPageFilter::isDisabled)
                .toMap();
        Page<CustomerEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(customerMapper.selectPage(page, buildPageWrapper(params)), DEFAULT_SORT);
    }

    @Override
    public CustomerEntity getDetail(Long id) {
        return getRequiredCustomer(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerEntity add(CustomerEntity customer) {
        validateCustomer(customer, false);
        initAddEntity(customer);
        checkDuplicate(customer.getName(), customer.getCode(), null);
        customerMapper.insert(customer);
        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerEntity update(CustomerEntity customer) {
        validateCustomer(customer, true);
        CustomerEntity exist = getRequiredCustomer(customer.getId());
        checkEditable(exist);
        checkDuplicate(customer.getName(), customer.getCode(), customer.getId());
        initUpdateEntity(customer);
        if (customer.getIsDisabled() == null) {
            customer.setIsDisabled(exist.getIsDisabled());
        }
        customerMapper.updateById(customer);
        return getDetail(customer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CustomerEntity exist = getRequiredCustomer(id);
        checkEditable(exist);
        customerMapper.deleteById(id);
    }

    private CustomerEntity getRequiredCustomer(Long id) {
        requireId(id, "客户ID不能为空");
        CustomerEntity customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "客户不存在");
        }
        return customer;
    }

    private void validateCustomer(CustomerEntity customer, boolean requireId) {
        requireNotNull(customer, "请求参数不能为空");
        if (requireId) {
            requireId(customer.getId(), "客户ID不能为空");
        }
        requireText(customer.getName(), "客户名称不能为空");
        customer.setName(trimToNull(customer.getName()));
        customer.setCode(trimToNull(customer.getCode()));
        customer.setPhone(trimToNull(customer.getPhone()));
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        LambdaQueryWrapper<CustomerEntity> wrapper = Wrappers.lambdaQuery(CustomerEntity.class)
                .and(query -> query.eq(CustomerEntity::getName, name).or().eq(CustomerEntity::getCode, code));
        if (excludeId != null) {
            wrapper.ne(CustomerEntity::getId, excludeId);
        }
        if (customerMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "客户名称或编码已存在");
        }
    }

    private LambdaQueryWrapper<CustomerEntity> buildPageWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");
        String code = (String) params.get("code");
        String phone = (String) params.get("phone");
        Boolean isDisabled = (Boolean) params.get("isDisabled");
        return Wrappers.lambdaQuery(CustomerEntity.class)
                .like(name != null, CustomerEntity::getName, name)
                .like(code != null, CustomerEntity::getCode, code)
                .like(phone != null, CustomerEntity::getPhone, phone)
                .eq(isDisabled != null, CustomerEntity::getIsDisabled, isDisabled)
                .orderByDesc(CustomerEntity::getId);
    }
}
