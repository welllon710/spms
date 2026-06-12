package com.spms.asset.service;

import com.spms.asset.entity.DeviceEntity;
import com.spms.asset.model.DevicePageFilter;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;

public interface DeviceService {
    PageResult<DeviceEntity> getPage(PageQuery<DevicePageFilter> request);

    DeviceEntity getDetail(Long id);

    DeviceEntity add(DeviceEntity device);

    DeviceEntity update(DeviceEntity device);

    void delete(Long id);
}
