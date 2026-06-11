package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.model.CustomerPageFilter;
import com.spms.common.result.PageResult;

public interface CustomerService {
    PageResult<CustomerEntity> getPage(PageQuery<CustomerPageFilter> request);

    CustomerEntity getDetail(Long id);

    CustomerEntity add(CustomerEntity customer);

    CustomerEntity update(CustomerEntity customer);

    void delete(Long id);
}
