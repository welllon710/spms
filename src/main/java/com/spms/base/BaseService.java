package com.spms.base;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;

import static com.spms.common.util.ParamUtils.requireNotNull;

public abstract class BaseService<E extends BaseEntity> {
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    protected void initAddEntity(E entity) {
        requireNotNull(entity, "请求参数不能为空");
        long now = System.currentTimeMillis();
        entity.setId(null);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDisabled(Boolean.TRUE.equals(entity.getIsDisabled()));
        entity.setIsPublished(false);
    }

    protected void initUpdateEntity(E entity) {
        requireNotNull(entity, "请求参数不能为空");
        entity.setUpdateTime(System.currentTimeMillis());
    }

    protected void checkEditable(E entity) {
        requireNotNull(entity, "请求参数不能为空");
        if (Boolean.TRUE.equals(entity.getIsPublished())) {
            throw new AppException(CommonError.FORBIDDEN, "无法修改或删除已经发布的数据");
        }
    }

    protected int getPageNum(PageQuery<?> request) {
        if (request == null || request.pageNum() == null || request.pageNum() < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return request.pageNum();
    }

    protected int getPageSize(PageQuery<?> request) {
        if (request == null || request.pageSize() == null || request.pageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(request.pageSize(), MAX_PAGE_SIZE);
    }
}
