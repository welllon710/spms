package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.OutputEntity;
import com.spms.wms.model.OutputAddRequest;
import com.spms.wms.model.OutputFinishRequest;
import com.spms.wms.model.OutputUpdateRequest;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.wms.model.OutputPageFilter;

public interface OutputService {
    PageResult<OutputEntity> getPage(PageQuery<OutputPageFilter> request);

    void add(OutputAddRequest request);

    void update(OutputUpdateRequest request);

    OutputEntity getDetail(IdRequest request);

    void audit(IdRequest request);

    void reject(RejectRequest request);

    void addFinish(OutputFinishRequest request);
}
