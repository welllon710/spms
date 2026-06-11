package com.spms.asset.service;

import com.spms.asset.entity.MaterialEntity;
import com.spms.asset.model.MaterialPageFilter;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;

public interface MaterialService {
    PageResult<MaterialEntity> getPage(PageQuery<MaterialPageFilter> request);

    MaterialEntity getDetail(Long id);

    MaterialEntity add(MaterialEntity material);

    MaterialEntity update(MaterialEntity material);

    void delete(Long id);
}
