package com.spms.channel.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.model.CustomerPageFilter;
import com.spms.channel.service.CustomerService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("customer")
@Permission
@RequiredArgsConstructor
public class CustomerController extends ApiController {
    private final CustomerService customerService;

    @PostMapping("/getPage")
    public Json<PageResult<CustomerEntity>> getPage(@RequestBody(required = false) PageQuery<CustomerPageFilter> request) {
        return Json.data(customerService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<CustomerEntity> getDetail(@RequestBody CustomerEntity customer) {
        return Json.data(customerService.getDetail(getCustomerId(customer)));
    }

    @PostMapping("/add")
    public Json<CustomerEntity> add(@RequestBody CustomerEntity customer) {
        return Json.data(customerService.add(customer), "新增成功");
    }

    @PostMapping("/update")
    public Json<CustomerEntity> update(@RequestBody CustomerEntity customer) {
        return Json.data(customerService.update(customer), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody CustomerEntity customer) {
        customerService.delete(getCustomerId(customer));
        return Json.success("删除成功");
    }

    private Long getCustomerId(CustomerEntity customer) {
        return customer == null ? null : customer.getId();
    }
}
